package com.multimarket.services.impl;

import com.multimarket.dto.AdminDashboardResponse;
import com.multimarket.models.*;
import com.multimarket.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private VendedorRepository vendedorRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private PedidoRepository pedidoRepository;
    @Mock private PagoRepository pagoRepository;
    @Mock private ConversacionRepository conversacionRepository;
    @Mock private InventarioRepository inventarioRepository;
    @Mock private LogSistemaRepository logSistemaRepository;
    @Mock private TransaccionSOAPRepository transaccionSOAPRepository;

    @InjectMocks
    private AdminDashboardServiceImpl service;

    @Test
    void getSummaryShouldAggregateKeyMetrics() {
        Usuario user = new Usuario();
        user.setId(1L);
        user.setCorreo("admin@test.com");
        user.setRoles(Set.of());

        Vendedor vendedor = new Vendedor();
        vendedor.setId(2L);
        vendedor.setNombreTienda("Tienda QA");
        vendedor.setRegion("Lima");
        vendedor.setActivo(true);

        Producto producto = new Producto();
        producto.setId(3L);
        producto.setActivo(true);
        producto.setVendedor(vendedor);

        Categoria categoria = new Categoria();
        categoria.setNombre("Cafe");
        producto.setCategoria(categoria);

        DetallePedido detalle = new DetallePedido();
        detalle.setProducto(producto);
        detalle.setSubtotal(BigDecimal.valueOf(120));

        Pedido pedido = new Pedido();
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setTotal(BigDecimal.valueOf(120));
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setVendedor(vendedor);
        pedido.setDetalles(List.of(detalle));

        Pago pago = new Pago();
        pago.setEstadoPago(EstadoPago.PENDIENTE);

        Conversacion conversacion = new Conversacion();
        conversacion.setActiva(true);

        Inventario inventario = new Inventario();
        inventario.setStockActual(1);
        inventario.setStockMinimo(5);

        LogSistema log = new LogSistema();
        log.setFechaHora(LocalDateTime.now());
        log.setAccion("QA");
        log.setModulo(ModuloSistema.KAFKA);
        log.setExitoso(true);
        log.setNivel(NivelLog.INFO);
        log.setUsuarioId(1L);

        TransaccionSOAP soap = new TransaccionSOAP();
        soap.setFecha(LocalDateTime.now());
        soap.setEstado("OK");

        when(usuarioRepository.findAll()).thenReturn(List.of(user));
        when(vendedorRepository.findAll()).thenReturn(List.of(vendedor));
        when(productoRepository.findAll()).thenReturn(List.of(producto));
        when(pedidoRepository.findAll()).thenReturn(List.of(pedido));
        when(pagoRepository.findAll()).thenReturn(List.of(pago));
        when(conversacionRepository.findAll()).thenReturn(List.of(conversacion));
        when(inventarioRepository.findAll()).thenReturn(List.of(inventario));
        when(logSistemaRepository.findAll()).thenReturn(List.of(log));
        when(transaccionSOAPRepository.findAll()).thenReturn(List.of(soap));
        when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

        AdminDashboardResponse response = service.getSummary();

        assertNotNull(response);
        assertFalse(response.kpis().isEmpty());
        assertEquals(1, response.kpis().stream().filter(k -> k.titulo().equals("Usuarios Totales")).findFirst().get().valor().equals("1") ? 1 : 0);
        assertEquals(1L, response.criticalAlerts().stockBajo());
        assertEquals(1L, response.kafkaStatus().mensajesHoy());
        assertEquals(1L, response.soapStatus().transaccionesHoy());
        assertEquals(1L, response.logsSummary().info());
    }
}
