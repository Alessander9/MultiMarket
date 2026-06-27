package com.multimarket.services.impl;

import com.multimarket.dto.VendedorRequest;
import com.multimarket.models.Rol;
import com.multimarket.models.RolNombre;
import com.multimarket.models.Usuario;
import com.multimarket.models.Vendedor;
import com.multimarket.repositories.UsuarioRepository;
import com.multimarket.repositories.VendedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendedorServiceImplTest {

    @Mock
    private VendedorRepository vendedorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private VendedorServiceImpl service;

    private Usuario vendedorUser;
    private Vendedor tienda;

    @BeforeEach
    void setUp() {
        vendedorUser = new Usuario();
        vendedorUser.setId(1L);
        vendedorUser.setCorreo("seller@test.com");
        Rol rol = new Rol();
        rol.setNombre(RolNombre.VENDEDOR);
        vendedorUser.setRoles(Set.of(rol));

        tienda = new Vendedor();
        tienda.setId(10L);
        tienda.setUsuario(vendedorUser);
        tienda.setNombreTienda("Tienda QA");
        tienda.setRegion("Lima");
        tienda.setDireccion("Av QA 123");
        tienda.setDescripcion("Demo");
        tienda.setLogo("logo.png");
        tienda.setBanner("banner.png");
        tienda.setActivo(true);
        tienda.setCalificacionPromedio(BigDecimal.valueOf(4.5));
    }

    @Test
    void editarTiendaAsAdminShouldAllowAnyStore() {
        Usuario admin = new Usuario();
        admin.setId(2L);
        admin.setCorreo("admin@test.com");
        Rol rol = new Rol();
        rol.setNombre(RolNombre.ADMIN);
        admin.setRoles(Set.of(rol));

        VendedorRequest request = new VendedorRequest();
        request.setNombreTienda("Tienda QA Editada");
        request.setDescripcion("Editada");
        request.setRegion("Cusco");
        request.setDireccion("Av QA 456");
        request.setLogo("logo2.png");
        request.setBanner("banner2.png");

        when(vendedorRepository.findById(10L)).thenReturn(Optional.of(tienda));
        when(vendedorRepository.findByNombreTienda("Tienda QA Editada")).thenReturn(Optional.empty());
        when(vendedorRepository.save(any(Vendedor.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.editarTienda(10L, admin.getCorreo(), true, request);

        assertEquals("Tienda QA Editada", response.getNombreTienda());
        assertEquals("Cusco", response.getRegion());
    }

    @Test
    void desactivarTiendaShouldRejectForeignSeller() {
        Usuario other = new Usuario();
        other.setId(3L);
        other.setCorreo("other@test.com");
        Rol rol = new Rol();
        rol.setNombre(RolNombre.VENDEDOR);
        other.setRoles(Set.of(rol));

        when(vendedorRepository.findById(10L)).thenReturn(Optional.of(tienda));

        SecurityException ex = assertThrows(SecurityException.class,
                () -> service.desactivarTienda(10L, other.getCorreo(), false, false));

        assertTrue(ex.getMessage().contains("No estás autorizado"));
    }

    @Test
    void desactivarTiendaAsAdminShouldUpdateState() {
        Usuario admin = new Usuario();
        admin.setId(8L);
        admin.setCorreo("admin@test.com");
        Rol rol = new Rol();
        rol.setNombre(RolNombre.ADMIN);
        admin.setRoles(Set.of(rol));

        when(vendedorRepository.findById(10L)).thenReturn(Optional.of(tienda));
        when(vendedorRepository.save(any(Vendedor.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.desactivarTienda(10L, admin.getCorreo(), true, false);

        assertFalse(response.getActivo());
    }

    @Test
    void listarTodosShouldMapResponses() {
        when(vendedorRepository.findAll()).thenReturn(List.of(tienda));

        var list = service.listarTodos();

        assertEquals(1, list.size());
        assertEquals("Tienda QA", list.get(0).getNombreTienda());
    }

    @Test
    void crearTiendaShouldRejectNonSellerRole() {
        Usuario comprador = new Usuario();
        comprador.setId(7L);
        comprador.setCorreo("buyer@test.com");
        Rol rol = new Rol();
        rol.setNombre(RolNombre.COMPRADOR);
        comprador.setRoles(Set.of(rol));

        VendedorRequest request = new VendedorRequest();
        request.setNombreTienda("Tienda No Permitida");
        request.setRegion("Lima");
        request.setDireccion("Av QA");

        when(usuarioRepository.findByCorreo("buyer@test.com")).thenReturn(Optional.of(comprador));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.crearTienda("buyer@test.com", request));

        assertTrue(ex.getMessage().contains("rol de VENDEDOR"));
    }

    @Test
    void editarTiendaShouldRejectForeignSellerWhenNotAdmin() {
        Usuario other = new Usuario();
        other.setId(9L);
        other.setCorreo("other@test.com");
        Rol rol = new Rol();
        rol.setNombre(RolNombre.VENDEDOR);
        other.setRoles(Set.of(rol));

        VendedorRequest request = new VendedorRequest();
        request.setNombreTienda("Otra Tienda");
        request.setRegion("Lima");
        request.setDireccion("Av QA");

        when(vendedorRepository.findById(10L)).thenReturn(Optional.of(tienda));

        SecurityException ex = assertThrows(SecurityException.class,
                () -> service.editarTienda(10L, other.getCorreo(), false, request));

        assertTrue(ex.getMessage().contains("No estás autorizado"));
    }
}
