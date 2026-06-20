package com.multimarket.services.impl;

import com.multimarket.models.*;
import com.multimarket.repositories.*;
import com.multimarket.kafka.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportacionServiceImplTest {

    @Mock private ImportacionCatalogoRepository importacionRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private VendedorRepository vendedorRepository;
    @Mock private InventarioRepository inventarioRepository;
    @Mock private KafkaProducer kafkaProducer;

    @InjectMocks
    private ImportacionServiceImpl service;

    @Test
    void importarCatalogoXmlShouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.xml", "text/xml", new byte[0]);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.importarCatalogoXml(file));
        assertTrue(ex.getMessage().contains("vacío"));
    }

    @Test
    void importarCatalogoXmlShouldPersistProductAndInventory() {
        String xml = """
                <catalogo>
                  <producto>
                    <nombre>Producto QA</nombre>
                    <descripcion>Demo</descripcion>
                    <sku>SKU-IMP-1</sku>
                    <precio>19.99</precio>
                    <stock>5</stock>
                    <peso>1.2</peso>
                    <categoriaId>1</categoriaId>
                    <vendedorId>2</vendedorId>
                  </producto>
                </catalogo>
                """;
        MockMultipartFile file = new MockMultipartFile("file", "catalogo.xml", "text/xml", xml.getBytes(StandardCharsets.UTF_8));

        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Cafe");
        categoria.setActiva(true);

        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setCorreo("seller@test.com");

        Vendedor vendedor = new Vendedor();
        vendedor.setId(2L);
        vendedor.setUsuario(usuario);
        vendedor.setNombreTienda("Tienda QA");

        when(productoRepository.findBySku("SKU-IMP-1")).thenReturn(Optional.empty());
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(vendedorRepository.findById(2L)).thenReturn(Optional.of(vendedor));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId(77L);
            return p;
        });
        when(importacionRepository.save(any(ImportacionCatalogo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.importarCatalogoXml(file);

        assertEquals(1, result.getTotalRegistros());
        assertEquals(1, result.getRegistrosCorrectos());
        assertEquals(0, result.getRegistrosError());
        verify(kafkaProducer, atLeastOnce()).sendLogEvent(any());
        assertTrue(new File(result.getRutaArchivo()).exists());
    }
}
