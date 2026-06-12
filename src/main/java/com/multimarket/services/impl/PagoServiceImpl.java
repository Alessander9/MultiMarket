package com.multimarket.services.impl;

import com.multimarket.dto.PagoRequest;
import com.multimarket.dto.PagoResponse;
import com.multimarket.dto.SoapTransactionResponse;
import com.multimarket.models.*;
import com.multimarket.repositories.PagoRepository;
import com.multimarket.repositories.PedidoRepository;
import com.multimarket.repositories.TransaccionSOAPRepository;
import com.multimarket.services.Interfaces.PagoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final TransaccionSOAPRepository transaccionSOAPRepository;

    public PagoServiceImpl(PagoRepository pagoRepository,
                           PedidoRepository pedidoRepository,
                           TransaccionSOAPRepository transaccionSOAPRepository) {
        this.pagoRepository = pagoRepository;
        this.pedidoRepository = pedidoRepository;
        this.transaccionSOAPRepository = transaccionSOAPRepository;
    }

    @Override
    public PagoResponse procesarPago(String compradorCorreo, PagoRequest request) {
        // 1. Validar Pedido
        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el pedido con el ID: " + request.getPedidoId()));

        // 2. Validar que el comprador logueado sea el dueño del pedido
        if (!pedido.getComprador().getCorreo().equals(compradorCorreo)) {
            throw new SecurityException("Acceso Denegado: No puedes pagar un pedido que no te pertenece.");
        }

        // 3. Validar estado del pedido
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalArgumentException("Solo se pueden pagar pedidos en estado PENDIENTE. Estado actual: " + pedido.getEstado());
        }

        // 4. Ejecutar Simulación de validación de tarjeta y procesamiento SOAP JAX-WS
        boolean tarjetaValida = true;
        if (request.getMetodoPago() == MetodoPago.VISA || request.getMetodoPago() == MetodoPago.MASTERCARD) {
            tarjetaValida = validarTarjeta(request.getNumeroTarjeta(), request.getCvv(), request.getFechaExpiracion());
        }

        if (!tarjetaValida) {
            throw new IllegalArgumentException("Error de Transacción: Tarjeta inválida o rechazada por la entidad bancaria.");
        }

        // Ejecutar simulación de pago SOAP
        String mockCardNumber = request.getNumeroTarjeta() != null ? request.getNumeroTarjeta() : "1111-2222-3333-4444";
        String codigoOperacion = procesarPagoSOAP(mockCardNumber, pedido.getTotal());

        if (codigoOperacion.equals("RECHAZADO") || request.getCvv() != null && request.getCvv().equals("999")) {
            throw new IllegalArgumentException("Error de Transacción: Pago rechazado por fondos insuficientes.");
        }

        // 5. Crear el registro de Pago Aprobado
        Pago pago = new Pago();
        pago.setMonto(pedido.getTotal());
        pago.setMetodoPago(request.getMetodoPago());
        pago.setEstadoPago(EstadoPago.APROBADO);
        pago.setCodigoOperacion(codigoOperacion);
        pago.setPedido(pedido);
        pago.setFechaPago(LocalDateTime.now());
        
        Pago savedPago = pagoRepository.save(pago);

        // 6. Actualizar el estado del pedido a PAGADO
        pedido.setEstado(EstadoPedido.PAGADO);
        pedidoRepository.save(pedido);

        return mapToResponse(savedPago);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse consultarPago(Long id, String usuarioCorreo) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el registro de pago con el ID: " + id));

        // Regla de seguridad: solo el comprador o el vendedor pueden consultar el pago
        boolean esComprador = pago.getPedido().getComprador().getCorreo().equals(usuarioCorreo);
        boolean esVendedor = pago.getPedido().getVendedor().getUsuario().getCorreo().equals(usuarioCorreo);

        if (!esComprador && !esVendedor) {
            throw new SecurityException("Acceso Denegado: No tienes permisos para consultar los detalles de este pago.");
        }

        return mapToResponse(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPagos() {
        return pagoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SoapTransactionResponse> listarTransaccionesSOAP() {
        return transaccionSOAPRepository.findAll().stream()
                .map(tx -> new SoapTransactionResponse(
                        tx.getId(),
                        tx.getRequestXml(),
                        tx.getResponseXml(),
                        tx.getFecha(),
                        tx.getEstado()
                ))
                .collect(Collectors.toList());
    }

    // ==========================================
    // SIMULACIÓN SOAP JAX-WS (Emulación con almacenamiento de XML)
    // ==========================================

    @Override
    public boolean validarTarjeta(String numeroTarjeta, String cvv, String fechaExpiracion) {
        String soapRequestXml = 
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://multimarket.com/payment\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <web:validarTarjeta>\n" +
            "         <web:numeroTarjeta>" + numeroTarjeta + "</web:numeroTarjeta>\n" +
            "         <web:cvv>" + cvv + "</web:cvv>\n" +
            "         <web:fechaExpiracion>" + fechaExpiracion + "</web:fechaExpiracion>\n" +
            "      </web:validarTarjeta>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

        boolean result = (numeroTarjeta != null && numeroTarjeta.replace("-", "").trim().length() >= 15);

        String soapResponseXml = 
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "   <soapenv:Body>\n" +
            "      <web:validarTarjetaResponse xmlns:web=\"http://multimarket.com/payment\">\n" +
            "         <web:valida>" + result + "</web:valida>\n" +
            "      </web:validarTarjetaResponse>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

        // Auditar transacción SOAP
        TransaccionSOAP trans = new TransaccionSOAP();
        trans.setRequestXml(soapRequestXml);
        trans.setResponseXml(soapResponseXml);
        trans.setEstado(result ? "VALIDACION_EXITOSA" : "VALIDACION_RECHAZADA");
        trans.setFecha(LocalDateTime.now());
        transaccionSOAPRepository.save(trans);

        return result;
    }

    @Override
    public String procesarPagoSOAP(String numeroTarjeta, BigDecimal monto) {
        String soapRequestXml = 
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://multimarket.com/payment\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <web:procesarPago>\n" +
            "         <web:numeroTarjeta>" + numeroTarjeta + "</web:numeroTarjeta>\n" +
            "         <web:monto>" + monto + "</web:monto>\n" +
            "      </web:procesarPago>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

        String codigoOperacion = "OP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        boolean aprobado = true;

        // Simulamos rechazo si el monto es sospechosamente alto
        if (monto.compareTo(BigDecimal.valueOf(100000)) > 0) {
            codigoOperacion = "RECHAZADO";
            aprobado = false;
        }

        String soapResponseXml = 
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "   <soapenv:Body>\n" +
            "      <web:procesarPagoResponse xmlns:web=\"http://multimarket.com/payment\">\n" +
            "         <web:codigoOperacion>" + codigoOperacion + "</web:codigoOperacion>\n" +
            "         <web:estado>" + (aprobado ? "APROBADO" : "RECHAZADO") + "</web:estado>\n" +
            "      </web:procesarPagoResponse>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

        // Auditar transacción SOAP
        TransaccionSOAP trans = new TransaccionSOAP();
        trans.setRequestXml(soapRequestXml);
        trans.setResponseXml(soapResponseXml);
        trans.setEstado(aprobado ? "PAGO_APROBADO" : "PAGO_RECHAZADO");
        trans.setFecha(LocalDateTime.now());
        transaccionSOAPRepository.save(trans);

        return codigoOperacion;
    }

    @Override
    public String consultarOperacionSOAP(String codigoOperacion) {
        String soapRequestXml = 
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://multimarket.com/payment\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <web:consultarOperacion>\n" +
            "         <web:codigoOperacion>" + codigoOperacion + "</web:codigoOperacion>\n" +
            "      </web:consultarOperacion>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

        String soapResponseXml = 
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "   <soapenv:Body>\n" +
            "      <web:consultarOperacionResponse xmlns:web=\"http://multimarket.com/payment\">\n" +
            "         <web:existe>true</web:existe>\n" +
            "         <web:estado>PROCESADO</web:estado>\n" +
            "      </web:consultarOperacionResponse>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

        TransaccionSOAP trans = new TransaccionSOAP();
        trans.setRequestXml(soapRequestXml);
        trans.setResponseXml(soapResponseXml);
        trans.setEstado("CONSULTA_OPERACION");
        trans.setFecha(LocalDateTime.now());
        transaccionSOAPRepository.save(trans);

        return "PROCESADO";
    }

    private PagoResponse mapToResponse(Pago p) {
        return new PagoResponse(
                p.getId(),
                p.getMonto(),
                p.getMetodoPago().name(),
                p.getEstadoPago().name(),
                p.getFechaPago(),
                p.getCodigoOperacion(),
                p.getPedido().getId(),
                p.getPedido().getNumeroPedido()
        );
    }
}
