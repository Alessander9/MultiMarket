package com.multimarket.controllers;

import com.multimarket.dto.AdminCreateUserRequest;
import com.multimarket.dto.AdminUserResponse;
import com.multimarket.repositories.UsuarioRepository;
import com.multimarket.services.Interfaces.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AuthService authService;

    @Test
    void crearUsuarioShouldDelegateToAdminService() {
        UsuarioController controller = new UsuarioController(usuarioRepository, authService);

        AdminUserResponse response = new AdminUserResponse();
        response.setId(55L);
        response.setCorreo("cleoferreteria@gmail.com");
        response.setRoles(List.of("VENDEDOR"));
        response.setFechaRegistro(LocalDateTime.now());
        when(authService.createAdminUser(any(AdminCreateUserRequest.class))).thenReturn(response);

        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setCorreo("cleoferreteria@gmail.com");
        request.setPassword("Secret123");

        var result = controller.crearUsuario(request);

        assertEquals(55L, result.getBody().getId());
        assertEquals("cleoferreteria@gmail.com", result.getBody().getCorreo());
    }
}
