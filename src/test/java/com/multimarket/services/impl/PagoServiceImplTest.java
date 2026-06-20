package com.multimarket.services.impl;

import com.multimarket.dto.PagoRequest;
import com.multimarket.models.*;
import com.multimarket.repositories.*;
import com.multimarket.services.Interfaces.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceImplTest {

    @Mock private PagoRepository pagoRepository;
    @Mock private PedidoRepository pedidoRepository;
    @Mock private TransaccionSOAPRepository transaccionSOAPRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private NotificacionService notificacionService;

    @InjectMocks
    private PagoServiceImpl service;

    private Usuario comprador;
    private Usuario vendedorUser;
    private Usuario admin;
    private Vendedor vendedor;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        comprador = new Usuario();
        comprador.setId(1L);
        comprador.setCorreo("buyer@test.com");

        vendedorUser = new Usuario();
        vendedorUser.setId(2L);
        vendedorUser.setCorreo("seller@test.com");

        admin = new Usuario();
        admin.setId(3L);
        admin.setCorreo("admin@test.com");
        Rol rolAdmin = new Rol();
        rolAdmin.setNombre(RolNombre.ADMIN);
        admin.setRoles(Set.of(rolAdmin));

        vendedor = new Vendedor();
        vendedor.setId(10L);
        vendedor.setUsuario(vendedorUser);
        vendedor.setNombreTienda("Tienda QA");

        pedido = new Pedido();
        pedido.setId(100L);
        pedido.setNumeroPedido("PED-100");
        pedido.setComprador(comprador);
        pedido.setVendedor(vendedor);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setTotal(BigDecimal.valueOf(250));
        pedido.setFechaPedido(LocalDateTime.now());
    }

    @Test
    void procesarPagoShouldApprovePendingOrderAndCreateNotifications() {
        PagoRequest request = new PagoRequest();
        request.setPedidoId(100L);
        request.setMetodoPago(MetodoPago.VISA);
        request.setNumeroTarjeta("4111-1111-1111-1111");
        request.setCvv("123");
        request.setFechaExpiracion("12/30");

        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedido));
        when(transaccionSOAPRepository.save(any(TransaccionSOAP.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> {
            Pago p = inv.getArgument(0);
            p.setId(50L);
            return p;
        });
        when(usuarioRepository.findAllByRoleNombre(RolNombre.ADMIN)).thenReturn(List.of(admin));
        when(notificacionService.generarNotificacion(any(), any(), any(), any()))
                .thenAnswer(inv -> {
                    Notificacion notif = new Notificacion();
                    notif.setId(1L);
                    return notif;
                });

        var response = service.procesarPago("buyer@test.com", request);

        assertEquals("APROBADO", response.getEstadoPago());
        assertEquals("PED-100", response.getNumeroPedido());
        verify(pagoRepository).save(any(Pago.class));
        verify(pedidoRepository).save(any(Pedido.class));
        verify(notificacionService, atLeastOnce()).generarNotificacion(any(), any(), any(), any());
    }

    @Test
    void procesarPagoShouldRejectHighAmountBySoapSimulation() {
        pedido.setTotal(BigDecimal.valueOf(200000));
        PagoRequest request = new PagoRequest();
        request.setPedidoId(100L);
        request.setMetodoPago(MetodoPago.TRANSFERENCIA);

        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedido));
        when(transaccionSOAPRepository.save(any(TransaccionSOAP.class))).thenAnswer(inv -> inv.getArgument(0));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.procesarPago("buyer@test.com", request));

        assertTrue(ex.getMessage().contains("rechazado"));
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void consultarPagoShouldRejectForeignUser() {
        Pago pago = new Pago();
        pago.setId(50L);
        pago.setMonto(BigDecimal.valueOf(100));
        pago.setMetodoPago(MetodoPago.YAPE);
        pago.setEstadoPago(EstadoPago.APROBADO);
        pago.setFechaPago(LocalDateTime.now());
        pago.setCodigoOperacion("OP-123");
        pago.setPedido(pedido);

        when(pagoRepository.findById(50L)).thenReturn(Optional.of(pago));

        SecurityException ex = assertThrows(SecurityException.class,
                () -> service.consultarPago(50L, "other@test.com"));

        assertTrue(ex.getMessage().contains("Acceso Denegado"));
    }

    @Test
    void listarTransaccionesSoapShouldMapEntities() {
        TransaccionSOAP tx = new TransaccionSOAP();
        tx.setId(1L);
        tx.setRequestXml("<req/>");
        tx.setResponseXml("<res/>");
        tx.setEstado("PAGO_APROBADO");
        tx.setFecha(LocalDateTime.now());

        when(transaccionSOAPRepository.findAll()).thenReturn(List.of(tx));

        var list = service.listarTransaccionesSOAP();

        assertEquals(1, list.size());
        assertEquals("PAGO_APROBADO", list.get(0).getEstado());
    }
}
