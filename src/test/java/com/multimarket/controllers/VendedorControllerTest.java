package com.multimarket.controllers;

import com.multimarket.dto.ProductoResponse;
import com.multimarket.dto.VendedorRequest;
import com.multimarket.dto.VendedorResponse;
import com.multimarket.services.Interfaces.ProductoService;
import com.multimarket.services.Interfaces.VendedorService;
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
class VendedorControllerTest {

    @Mock private VendedorService vendedorService;
    @Mock private ProductoService productoService;

    @Test
    void editVendorShouldReturnUpdatedStore() {
        VendedorController controller = new VendedorController(vendedorService, productoService);
        VendedorResponse response = new VendedorResponse(10L, 1L, "Tienda QA Editada", "Demo", "Lima", "Av QA 123", "logo.png", "banner.png", LocalDateTime.now(), true, BigDecimal.valueOf(4.5));
        when(vendedorService.editarTienda(eq(10L), any(), eq(true), any())).thenReturn(response);

        UserDetails admin = new User("admin@test.com", "x", Set.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        VendedorRequest request = new VendedorRequest();
        request.setNombreTienda("Tienda QA Editada");
        request.setDescripcion("Demo");
        request.setRegion("Lima");
        request.setDireccion("Av QA 123");
        request.setLogo("logo.png");
        request.setBanner("banner.png");

        var result = controller.editarTienda(10L, request, admin);

        assertEquals("Tienda QA Editada", result.getBody().getNombreTienda());
        assertEquals(true, result.getBody().getActivo());
    }

    @Test
    void desactivarTiendaShouldDelegateAdminFlagAndState() {
        VendedorController controller = new VendedorController(vendedorService, productoService);
        VendedorResponse response = new VendedorResponse(10L, 1L, "Tienda QA", "Demo", "Lima", "Av QA 123", "logo.png", "banner.png", LocalDateTime.now(), false, BigDecimal.valueOf(4.5));
        when(vendedorService.desactivarTienda(eq(10L), eq("admin@test.com"), eq(true), eq(false))).thenReturn(response);

        UserDetails admin = new User("admin@test.com", "x", Set.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        var result = controller.desactivarTienda(10L, false, admin);

        assertEquals(false, result.getBody().getActivo());
        assertEquals("Tienda QA", result.getBody().getNombreTienda());
    }

    @Test
    void listarProductosMiTiendaShouldUseStoreId() {
        VendedorController controller = new VendedorController(vendedorService, productoService);
        VendedorResponse store = new VendedorResponse(9L, 1L, "Tienda QA", "Demo", "Lima", "Av QA", "logo", "banner", LocalDateTime.now(), true, BigDecimal.ONE);
        ProductoResponse product = new ProductoResponse();
        product.setId(1L);
        product.setNombre("Producto QA");

        when(vendedorService.consultarMiTienda("seller@test.com")).thenReturn(store);
        when(productoService.listarProductosPorTienda(9L)).thenReturn(List.of(product));

        UserDetails seller = new User("seller@test.com", "x", Set.of(new SimpleGrantedAuthority("ROLE_VENDEDOR")));
        var result = controller.listarProductosMiTienda(seller);

        assertEquals(1, result.getBody().size());
        assertEquals("Producto QA", result.getBody().get(0).getNombre());
    }
}
