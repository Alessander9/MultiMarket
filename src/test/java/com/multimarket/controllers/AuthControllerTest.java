package com.multimarket.controllers;

import com.multimarket.dto.LoginRequest;
import com.multimarket.dto.LoginResponse;
import com.multimarket.dto.UserProfileResponse;
import com.multimarket.services.Interfaces.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Test
    void loginShouldReturnJwtPayload() {
        AuthController controller = new AuthController(authService);
        LoginResponse response = new LoginResponse();
        response.setToken("jwt-token");
        response.setCorreo("admin@test.com");
        response.setRoles(Set.of("ADMIN"));
        when(authService.login(any(), any(), any())).thenReturn(response);

        LoginRequest request = new LoginRequest();
        request.setCorreo("admin@test.com");
        request.setPassword("Admin123");

        var result = controller.login(request, new MockHttpServletRequest());

        assertEquals("jwt-token", result.getBody().getToken());
        assertEquals("admin@test.com", result.getBody().getCorreo());
        assertEquals(Set.of("ADMIN"), result.getBody().getRoles());
    }

    @Test
    void profileShouldReturnCurrentUserProfile() {
        AuthController controller = new AuthController(authService);
        UserProfileResponse profile = new UserProfileResponse();
        profile.setCorreo("admin@test.com");
        profile.setNombres("Admin");
        when(authService.getProfile("admin@test.com")).thenReturn(profile);

        UserDetails userDetails = new User("admin@test.com", "x", Set.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        var result = controller.getProfile(userDetails);

        assertEquals("admin@test.com", result.getBody().getCorreo());
        assertEquals("Admin", result.getBody().getNombres());
    }
}
