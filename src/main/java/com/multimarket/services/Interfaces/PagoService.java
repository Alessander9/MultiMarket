package com.multimarket.services.Interfaces;

import com.multimarket.dto.CompraAgrupadaPagoRequest;
import com.multimarket.dto.CompraAgrupadaResponse;
import com.multimarket.dto.PagoRequest;
import com.multimarket.dto.PagoResponse;
import com.multimarket.dto.SoapTransactionResponse;
import java.util.List;
import java.math.BigDecimal;

public interface PagoService {
    // REST API Entrypoint
    PagoResponse procesarPago(String compradorCorreo, PagoRequest request);
    CompraAgrupadaResponse procesarCompraAgrupada(String compradorCorreo, CompraAgrupadaPagoRequest request);
    PagoResponse consultarPago(Long id, String usuarioCorreo);
    List<PagoResponse> listarPagos();
    List<PagoResponse> listarPagosPorVendedor(String vendedorCorreo);
    List<SoapTransactionResponse> listarTransaccionesSOAP();

    // SOAP Simulated Web Service Operations (JAX-WS emulation)
    boolean validarTarjeta(String numeroTarjeta, String cvv, String fechaExpiracion);
    String procesarPagoSOAP(String numeroTarjeta, BigDecimal monto);
    String consultarOperacionSOAP(String codigoOperacion);
}
