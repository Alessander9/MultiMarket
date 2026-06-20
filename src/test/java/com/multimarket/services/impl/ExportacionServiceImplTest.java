package com.multimarket.services.impl;

import com.multimarket.models.*;
import com.multimarket.repositories.ExportacionCatalogoRepository;
import com.multimarket.repositories.ProductoRepository;
import com.multimarket.kafka.KafkaProducer;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportacionServiceImplTest {

    @Mock private ExportacionCatalogoRepository exportacionRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private KafkaProducer kafkaProducer;

    @InjectMocks
    private ExportacionServiceImpl service;

    private long nextId = 1L;

    @Test
    void programarExportacionShouldPersistPendingExport() {
        when(exportacionRepository.save(any(ExportacionCatalogo.class))).thenAnswer(inv -> {
            ExportacionCatalogo e = inv.getArgument(0);
            e.setId(nextId++);
            return e;
        });

        var result = service.programarExportacion(FormatoExportacion.JSON);

        assertEquals(EstadoExportacion.PENDIENTE, result.getEstado());
        assertEquals(FormatoExportacion.JSON, result.getFormato());
        verify(exportacionRepository).save(any(ExportacionCatalogo.class));
    }

    @Test
    void asynchronousExportShouldCompleteForXml() throws Exception {
        ExportacionCatalogo export = new ExportacionCatalogo();
        export.setId(2L);
        export.setFormato(FormatoExportacion.XML);
        export.setEstado(EstadoExportacion.PENDIENTE);
        export.setFechaExportacion(LocalDateTime.now());

        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Cafe");
        categoria.setActiva(true);

        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setCorreo("seller@test.com");

        Vendedor vendedor = new Vendedor();
        vendedor.setId(3L);
        vendedor.setUsuario(usuario);
        vendedor.setNombreTienda("Tienda QA");

        Producto producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Producto QA");
        producto.setDescripcion("Demo");
        producto.setSku("SKU-EXP");
        producto.setPrecio(BigDecimal.valueOf(10));
        producto.setStock(5);
        producto.setPeso(BigDecimal.valueOf(1));
        producto.setCategoria(categoria);
        producto.setVendedor(vendedor);

        when(exportacionRepository.save(any(ExportacionCatalogo.class))).thenAnswer(inv -> {
            ExportacionCatalogo e = inv.getArgument(0);
            e.setId(nextId++);
            return e;
        });
        when(exportacionRepository.findById(any())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            return id != null && id.equals(1L) ? Optional.of(export) : Optional.empty();
        });
        when(productoRepository.findAll()).thenReturn(List.of(producto));

        var result = service.programarExportacion(FormatoExportacion.XML);

        assertEquals(EstadoExportacion.PENDIENTE, result.getEstado());
        Thread.sleep(800);
        verify(kafkaProducer, atLeastOnce()).sendLogEvent(any());
    }
}
