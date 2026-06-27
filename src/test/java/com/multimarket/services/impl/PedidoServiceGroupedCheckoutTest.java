package com.multimarket.services.impl;

import com.multimarket.dto.*;
import com.multimarket.models.*;
import com.multimarket.repositories.*;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceGroupedCheckoutTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private DetallePedidoRepository detallePedidoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private VendedorRepository vendedorRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private InventarioRepository inventarioRepository;
    @Mock private MovimientoInventarioRepository movimientoInventarioRepository;

    @InjectMocks
    private PedidoServiceImpl service;

    private Usuario comprador;
    private Usuario vendedorUser1;
    private Usuario vendedorUser2;
    private Vendedor tienda1;
    private Vendedor tienda2;
    private Producto prod1;
    private Producto prod2;

    @BeforeEach
    void setUp() {
        comprador = new Usuario();
        comprador.setId(1L);
        comprador.setCorreo("buyer@test.com");

        vendedorUser1 = new Usuario();
        vendedorUser1.setId(2L);
        vendedorUser1.setCorreo("seller1@test.com");

        vendedorUser2 = new Usuario();
        vendedorUser2.setId(3L);
        vendedorUser2.setCorreo("seller2@test.com");

        tienda1 = new Vendedor();
        tienda1.setId(10L);
        tienda1.setUsuario(vendedorUser1);
        tienda1.setNombreTienda("Tienda Uno");

        tienda2 = new Vendedor();
        tienda2.setId(11L);
        tienda2.setUsuario(vendedorUser2);
        tienda2.setNombreTienda("Tienda Dos");

        prod1 = new Producto();
        prod1.setId(100L);
        prod1.setNombre("Producto A");
        prod1.setPrecio(BigDecimal.valueOf(20));
        prod1.setStock(10);
        prod1.setActivo(true);
        prod1.setVendedor(tienda1);

        prod2 = new Producto();
        prod2.setId(200L);
        prod2.setNombre("Producto B");
        prod2.setPrecio(BigDecimal.valueOf(30));
        prod2.setStock(10);
        prod2.setActivo(true);
        prod2.setVendedor(tienda2);
    }

    @Test
    void crearPedidosAgrupadosShouldCreateOneOrderPerVendor() {
        CompraAgrupadaRequest request = new CompraAgrupadaRequest();

        GrupoPedidoRequest grupo1 = new GrupoPedidoRequest();
        grupo1.setVendedorId(10L);
        grupo1.setCostoEnvio(BigDecimal.valueOf(15));
        DetallePedidoRequest det1 = new DetallePedidoRequest();
        det1.setProductoId(100L);
        det1.setCantidad(2);
        grupo1.setDetalles(List.of(det1));

        GrupoPedidoRequest grupo2 = new GrupoPedidoRequest();
        grupo2.setVendedorId(11L);
        grupo2.setCostoEnvio(BigDecimal.valueOf(0));
        DetallePedidoRequest det2 = new DetallePedidoRequest();
        det2.setProductoId(200L);
        det2.setCantidad(1);
        grupo2.setDetalles(List.of(det2));

        request.setGrupos(List.of(grupo1, grupo2));

        when(usuarioRepository.findByCorreo("buyer@test.com")).thenReturn(Optional.of(comprador));
        when(vendedorRepository.findById(10L)).thenReturn(Optional.of(tienda1));
        when(vendedorRepository.findById(11L)).thenReturn(Optional.of(tienda2));
        when(productoRepository.findById(100L)).thenReturn(Optional.of(prod1));
        when(productoRepository.findById(200L)).thenReturn(Optional.of(prod2));
        when(inventarioRepository.findByProductoId(any())).thenReturn(Optional.empty());
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            if (p.getId() == null) {
                p.setId((long) (Math.random() * 1000));
            }
            return p;
        });

        var response = service.crearPedidosAgrupados("buyer@test.com", request);

        assertEquals(2, response.getPedidos().size());
        assertEquals(BigDecimal.valueOf(70).setScale(2), response.getSubtotal().setScale(2));
        assertEquals(2, response.getPedidos().stream().map(PedidoResponse::getVendedorTienda).distinct().count());
    }

    @Test
    void crearPedidosAgrupadosShouldRejectMismatchedVendorProduct() {
        CompraAgrupadaRequest request = new CompraAgrupadaRequest();
        GrupoPedidoRequest grupo = new GrupoPedidoRequest();
        grupo.setVendedorId(10L);
        grupo.setCostoEnvio(BigDecimal.valueOf(15));
        DetallePedidoRequest det = new DetallePedidoRequest();
        det.setProductoId(200L);
        det.setCantidad(1);
        grupo.setDetalles(List.of(det));
        request.setGrupos(List.of(grupo));

        when(usuarioRepository.findByCorreo("buyer@test.com")).thenReturn(Optional.of(comprador));
        when(vendedorRepository.findById(10L)).thenReturn(Optional.of(tienda1));
        when(productoRepository.findById(200L)).thenReturn(Optional.of(prod2));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.crearPedidosAgrupados("buyer@test.com", request));

        assertTrue(ex.getMessage().contains("no pertenece"));
    }
}
