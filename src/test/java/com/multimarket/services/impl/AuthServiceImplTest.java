package com.multimarket.services.impl;

import com.multimarket.dto.AdminCreateUserRequest;
import com.multimarket.dto.AdminUserResponse;
import com.multimarket.models.Perfil;
import com.multimarket.models.Rol;
import com.multimarket.models.RolNombre;
import com.multimarket.models.Usuario;
import com.multimarket.repositories.*;
import com.multimarket.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolRepository rolRepository;
    @Mock private PerfilRepository perfilRepository;
    @Mock private RecuperacionPasswordRepository recuperacionRepository;
    @Mock private VerificacionCorreoRepository verificacionRepository;
    @Mock private SesionUsuarioRepository sesionRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private UserDetailsService userDetailsService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl service;

    @Test
    void createAdminUserShouldPersistUserWithGeneratedProfile() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setCorreo("cleoferreteria@gmail.com");
        request.setPassword("Secret123");
        request.setRoles(Set.of(RolNombre.VENDEDOR));
        request.setEstado(true);

        Rol vendedorRol = new Rol();
        vendedorRol.setId(2L);
        vendedorRol.setNombre(RolNombre.VENDEDOR);
        vendedorRol.setDescripcion("Rol VENDEDOR");

        when(usuarioRepository.existsByCorreo("cleoferreteria@gmail.com")).thenReturn(false);
        when(rolRepository.findByNombre(RolNombre.VENDEDOR)).thenReturn(Optional.empty());
        when(rolRepository.save(any(Rol.class))).thenReturn(vendedorRol);
        when(passwordEncoder.encode("Secret123")).thenReturn("encoded-secret");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(99L);
            Perfil perfil = usuario.getPerfil();
            if (perfil != null) {
                perfil.setId(99L);
            }
            return usuario;
        });

        AdminUserResponse response = service.createAdminUser(request);

        assertEquals(99L, response.getId());
        assertEquals("cleoferreteria@gmail.com", response.getCorreo());
        assertEquals(Set.of("VENDEDOR"), Set.copyOf(response.getRoles()));
        assertEquals("Cleoferreteria", response.getNombres());
        assertEquals("Vendedor", response.getApellidos());
        assertTrue(response.getDni().startsWith("AUTO"));
        assertEquals(LocalDate.of(1990, 1, 1), response.getFechaNacimiento());
        assertTrue(response.getCorreoVerificado());
    }
}
