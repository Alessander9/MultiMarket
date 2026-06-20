package com.multimarket.controllers;

import com.multimarket.dto.ImagenProductoResponse;
import com.multimarket.dto.ProductoRequest;
import com.multimarket.dto.ProductoResponse;
import com.multimarket.services.Interfaces.ProductoService;
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
class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    @Test
    void createProductShouldUseAuthenticatedSellerEmail() {
        ProductoController controller = new ProductoController(productoService);
        ProductoResponse response = new ProductoResponse(1L, "Producto QA", "Demo", "SKU-1", BigDecimal.valueOf(19.9), 5, BigDecimal.valueOf(1.2), true, LocalDateTime.now(), LocalDateTime.now(), 3L, "Cafe", 9L, "Tienda QA", List.of());
        when(productoService.crearProducto(eq("seller@test.com"), any())).thenReturn(response);

        ProductoRequest request = new ProductoRequest();
        request.setNombre("Producto QA");
        request.setSku("SKU-1");
        request.setPrecio(BigDecimal.valueOf(19.9));
        request.setStock(5);
        request.setPeso(BigDecimal.valueOf(1.2));
        request.setCategoriaId(3L);

        UserDetails seller = new User("seller@test.com", "x", Set.of(new SimpleGrantedAuthority("ROLE_VENDEDOR")));

        var result = controller.crearProducto(request, seller);

        assertEquals("Producto QA", result.getBody().getNombre());
        assertEquals("Tienda QA", result.getBody().getTiendaNombre());
    }

    @Test
    void addImageShouldPassAuthenticatedUserAndFlags() {
        ProductoController controller = new ProductoController(productoService);
        ImagenProductoResponse image = new ImagenProductoResponse(7L, "img.png", true, 1);
        when(productoService.agregarImagen(1L, "seller@test.com", "img.png", true, 1)).thenReturn(image);

        UserDetails seller = new User("seller@test.com", "x", Set.of(new SimpleGrantedAuthority("ROLE_VENDEDOR")));

        var result = controller.agregarImagen(1L, "img.png", true, 1, seller);

        assertEquals("img.png", result.getBody().getUrl());
        assertEquals(true, result.getBody().getPrincipal());
    }
}
