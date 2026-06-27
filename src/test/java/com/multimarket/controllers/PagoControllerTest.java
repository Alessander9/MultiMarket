package com.multimarket.controllers;

import com.multimarket.dto.CompraAgrupadaPagoRequest;
import com.multimarket.dto.CompraAgrupadaResponse;
import com.multimarket.dto.PedidoResponse;
import com.multimarket.dto.PagoRequest;
import com.multimarket.dto.PagoResponse;
import com.multimarket.dto.SoapTransactionResponse;
import com.multimarket.services.Interfaces.PagoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoControllerTest {

    @Mock private PagoService pagoService;

    @Test
    void procesarPagoShouldUseAuthenticatedBuyer() {
        PagoController controller = new PagoController(pagoService);
        PagoRequest request = new PagoRequest();
        request.setPedidoId(100L);
        request.setMetodoPago(com.multimarket.models.MetodoPago.VISA);

        when(pagoService.procesarPago(eq("buyer@test.com"), any())).thenReturn(
                new PagoResponse(1L, BigDecimal.valueOf(250), "VISA", "APROBADO", LocalDateTime.now(), "OP-123", 100L, "PED-100")
        );

        UserDetails buyer = new User("buyer@test.com", "x", Set.of(new SimpleGrantedAuthority("ROLE_COMPRADOR")));

        var result = controller.procesarPago(request, buyer);

        assertEquals("APROBADO", result.getBody().getEstadoPago());
    }

    @Test
    void listarPagosShouldReturnServiceData() {
        PagoController controller = new PagoController(pagoService);
        when(pagoService.listarPagos()).thenReturn(List.of(
                new PagoResponse(1L, BigDecimal.valueOf(50), "YAPE", "APROBADO", LocalDateTime.now(), "OP-1", 1L, "PED-1")
        ));

        var result = controller.listarPagos();

        assertEquals(1, result.getBody().size());
        assertEquals("OP-1", result.getBody().get(0).getCodigoOperacion());
    }

    @Test
    void listarSoapTransactionsShouldReturnServiceData() {
        PagoController controller = new PagoController(pagoService);
        when(pagoService.listarTransaccionesSOAP()).thenReturn(List.of(
                new SoapTransactionResponse(1L, "<req/>", "<res/>", LocalDateTime.now(), "PAGO_APROBADO")
        ));

        var result = controller.listarTransaccionesSOAP();

        assertEquals(1, result.getBody().size());
        assertEquals("PAGO_APROBADO", result.getBody().get(0).getEstado());
    }

    @Test
    void procesarCompraAgrupadaShouldUseAuthenticatedBuyer() {
        PagoController controller = new PagoController(pagoService);
        CompraAgrupadaPagoRequest request = new CompraAgrupadaPagoRequest();
        request.setGrupos(List.of());
        request.setMetodoPago(com.multimarket.models.MetodoPago.VISA);

        when(pagoService.procesarCompraAgrupada(eq("buyer@test.com"), any())).thenReturn(
                new CompraAgrupadaResponse(
                        1L,
                        "CMP-100",
                        LocalDateTime.now(),
                        "VISA",
                        "PAGADO",
                        List.of(new PedidoResponse(1L, "PED-100", LocalDateTime.now(), BigDecimal.valueOf(100), BigDecimal.valueOf(18), BigDecimal.ZERO, BigDecimal.valueOf(118), "PAGADO", "buyer@test.com", 10L, "Tienda QA", List.of())),
                        BigDecimal.valueOf(100),
                        BigDecimal.valueOf(18),
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(118)
                )
        );

        UserDetails buyer = new User("buyer@test.com", "x", Set.of(new SimpleGrantedAuthority("ROLE_COMPRADOR")));

        var result = controller.procesarCompraAgrupada(request, buyer);

        assertEquals(1, result.getBody().getPedidos().size());
        assertEquals("PAGADO", result.getBody().getPedidos().get(0).getEstado());
    }
}
