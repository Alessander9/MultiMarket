package com.multimarket.services.impl;

import com.multimarket.dto.ProductoRequest;
import com.multimarket.models.Categoria;
import com.multimarket.models.Producto;
import com.multimarket.models.Rol;
import com.multimarket.models.RolNombre;
import com.multimarket.models.Usuario;
import com.multimarket.models.Vendedor;
import com.multimarket.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private VendedorRepository vendedorRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ImagenProductoRepository imagenRepository;
    @Mock private ProductoFavoritoRepository favoritoRepository;

    @InjectMocks
    private ProductoServiceImpl service;

    private Usuario vendedorUser;
    private Vendedor tienda;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        vendedorUser = new Usuario();
        vendedorUser.setId(1L);
        vendedorUser.setCorreo("seller@test.com");
        Rol rol = new Rol();
        rol.setNombre(RolNombre.VENDEDOR);
        vendedorUser.setRoles(Set.of(rol));

        tienda = new Vendedor();
        tienda.setId(20L);
        tienda.setUsuario(vendedorUser);
        tienda.setNombreTienda("Tienda QA");

        categoria = new Categoria();
        categoria.setId(30L);
        categoria.setNombre("Cafe");
        categoria.setActiva(true);
    }

    @Test
    void crearProductoAsSellerUsesOwnStore() {
        ProductoRequest request = new ProductoRequest();
        request.setNombre("Producto QA");
        request.setDescripcion("Demo");
        request.setSku("SKU-001");
        request.setPrecio(BigDecimal.valueOf(25.5));
        request.setStock(5);
        request.setPeso(BigDecimal.valueOf(1.2));
        request.setCategoriaId(30L);

        when(usuarioRepository.findByCorreo("seller@test.com")).thenReturn(Optional.of(vendedorUser));
        when(vendedorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(tienda));
        when(categoriaRepository.findById(30L)).thenReturn(Optional.of(categoria));
        when(productoRepository.findBySku("SKU-001")).thenReturn(Optional.empty());
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.crearProducto("seller@test.com", request);

        assertEquals("Producto QA", response.getNombre());
        assertEquals(20L, response.getVendedorId());
        assertEquals(30L, response.getCategoriaId());
    }

    @Test
    void crearProductoShouldRejectInactiveCategory() {
        categoria.setActiva(false);
        ProductoRequest request = new ProductoRequest();
        request.setNombre("Producto QA");
        request.setSku("SKU-002");
        request.setPrecio(BigDecimal.valueOf(10));
        request.setStock(1);
        request.setPeso(BigDecimal.valueOf(1));
        request.setCategoriaId(30L);

        when(usuarioRepository.findByCorreo("seller@test.com")).thenReturn(Optional.of(vendedorUser));
        when(vendedorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(tienda));
        when(categoriaRepository.findById(30L)).thenReturn(Optional.of(categoria));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.crearProducto("seller@test.com", request));

        assertTrue(ex.getMessage().contains("no se encuentra activa"));
    }

    @Test
    void desactivarProductoShouldRejectForeignSeller() {
        Usuario other = new Usuario();
        other.setId(99L);
        other.setCorreo("other@test.com");
        Rol rol = new Rol();
        rol.setNombre(RolNombre.VENDEDOR);
        other.setRoles(Set.of(rol));

        Producto producto = new Producto();
        producto.setId(88L);
        producto.setVendedor(tienda);

        when(usuarioRepository.findByCorreo("other@test.com")).thenReturn(Optional.of(other));
        when(productoRepository.findById(88L)).thenReturn(Optional.of(producto));

        SecurityException ex = assertThrows(SecurityException.class,
                () -> service.desactivarProducto(88L, "other@test.com"));

        assertTrue(ex.getMessage().contains("No estás autorizado"));
    }
}
