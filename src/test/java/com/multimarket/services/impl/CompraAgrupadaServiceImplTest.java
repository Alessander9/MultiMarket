package com.multimarket.services.impl;

import com.multimarket.dto.CompraAgrupadaResponse;
import com.multimarket.models.*;
import com.multimarket.repositories.CompraAgrupadaRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompraAgrupadaServiceImplTest {

    @Mock private CompraAgrupadaRepository compraAgrupadaRepository;

    @InjectMocks
    private CompraAgrupadaServiceImpl service;

    private CompraAgrupada compra;

    @BeforeEach
    void setUp() {
        Usuario comprador = new Usuario();
        comprador.setId(1L);
        comprador.setCorreo("buyer@test.com");

        Usuario vendedorUser = new Usuario();
        vendedorUser.setId(2L);
        vendedorUser.setCorreo("seller@test.com");

        Vendedor vendedor = new Vendedor();
        vendedor.setId(10L);
        vendedor.setNombreTienda("Tienda QA");
        vendedor.setUsuario(vendedorUser);

        Producto producto = new Producto();
        producto.setId(50L);
        producto.setNombre("Cafe QA");

        DetallePedido detalle = new DetallePedido();
        detalle.setId(70L);
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(BigDecimal.valueOf(25));
        detalle.setSubtotal(BigDecimal.valueOf(25));
        detalle.setProducto(producto);

        Pedido pedido = new Pedido();
        pedido.setId(100L);
        pedido.setNumeroPedido("PED-100");
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setSubtotal(BigDecimal.valueOf(25));
        pedido.setImpuesto(BigDecimal.valueOf(4.5));
        pedido.setCostoEnvio(BigDecimal.valueOf(15));
        pedido.setTotal(BigDecimal.valueOf(44.5));
        pedido.setEstado(EstadoPedido.PAGADO);
        pedido.setComprador(comprador);
        pedido.setVendedor(vendedor);
        pedido.setDetalles(List.of(detalle));

        compra = new CompraAgrupada();
        compra.setId(500L);
        compra.setNumeroCompra("CMP-20260621-QA01");
        compra.setFechaCompra(LocalDateTime.now());
        compra.setMetodoPago(MetodoPago.VISA);
        compra.setComprador(comprador);
        compra.setSubtotal(BigDecimal.valueOf(25));
        compra.setImpuesto(BigDecimal.valueOf(4.5));
        compra.setCostoEnvioTotal(BigDecimal.valueOf(15));
        compra.setTotal(BigDecimal.valueOf(44.5));
        compra.setPedidos(List.of(pedido));
    }

    @Test
    void listarComprasPorCompradorShouldMapGroupedPurchases() {
        when(compraAgrupadaRepository.findByCompradorCorreoOrderByFechaCompraDesc("buyer@test.com"))
                .thenReturn(List.of(compra));

        List<CompraAgrupadaResponse> result = service.listarComprasPorComprador("buyer@test.com");

        assertEquals(1, result.size());
        assertEquals("CMP-20260621-QA01", result.get(0).getNumeroCompra());
        assertEquals(1, result.get(0).getPedidos().size());
        assertEquals("Tienda QA", result.get(0).getPedidos().get(0).getVendedorTienda());
    }

    @Test
    void generarBoletaPdfShouldReturnPdfBytes() {
        when(compraAgrupadaRepository.findById(500L)).thenReturn(Optional.of(compra));

        byte[] pdf = service.generarBoletaPdf(500L, "buyer@test.com");

        assertNotNull(pdf);
        assertTrue(pdf.length > 100);
        assertEquals("%PDF", new String(pdf, 0, 4));
    }
}
