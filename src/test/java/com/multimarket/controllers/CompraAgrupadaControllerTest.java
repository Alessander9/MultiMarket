package com.multimarket.controllers;

import com.multimarket.dto.CompraAgrupadaResponse;
import com.multimarket.services.Interfaces.CompraAgrupadaService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompraAgrupadaControllerTest {

    @Mock private CompraAgrupadaService compraAgrupadaService;

    @Test
    void listarMisComprasShouldReturnBuyerPurchases() {
        CompraAgrupadaController controller = new CompraAgrupadaController(compraAgrupadaService);
        UserDetails buyer = new User("buyer@test.com", "x", Set.of(new SimpleGrantedAuthority("ROLE_COMPRADOR")));

        when(compraAgrupadaService.listarComprasPorComprador("buyer@test.com"))
                .thenReturn(List.of(new CompraAgrupadaResponse(
                        1L,
                        "CMP-100",
                        LocalDateTime.now(),
                        "VISA",
                        "PAGADO",
                        List.of(),
                        BigDecimal.valueOf(100),
                        BigDecimal.valueOf(18),
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(118)
                )));

        var result = controller.listarMisCompras(buyer);

        assertEquals(1, result.getBody().size());
        assertEquals("CMP-100", result.getBody().get(0).getNumeroCompra());
    }

    @Test
    void descargarBoletaPdfShouldReturnPdfPayload() {
        CompraAgrupadaController controller = new CompraAgrupadaController(compraAgrupadaService);
        UserDetails buyer = new User("buyer@test.com", "x", Set.of(new SimpleGrantedAuthority("ROLE_COMPRADOR")));
        byte[] pdf = "%PDF-demo".getBytes();

        when(compraAgrupadaService.generarBoletaPdf(99L, "buyer@test.com")).thenReturn(pdf);

        var result = controller.descargarBoletaPdf(99L, buyer);

        assertEquals(pdf.length, result.getBody().length);
        assertEquals("application/pdf", result.getHeaders().getContentType().toString());
    }
}
