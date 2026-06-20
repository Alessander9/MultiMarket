package com.multimarket.controllers;

import com.multimarket.dto.ConversacionRequest;
import com.multimarket.dto.ConversacionResponse;
import com.multimarket.dto.MensajeRequest;
import com.multimarket.dto.MensajeResponse;
import com.multimarket.services.Interfaces.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock private ChatService chatService;

    @Test
    void crearConversacionShouldUseAuthenticatedUser() {
        ChatController controller = new ChatController(chatService);
        UserDetails buyer = new User("buyer@test.com", "x", Set.of(new SimpleGrantedAuthority("ROLE_COMPRADOR")));

        ConversacionRequest request = new ConversacionRequest();
        request.setVendedorId(15L);

        ConversacionResponse response = new ConversacionResponse(1L, LocalDateTime.now(), true, "buyer@test.com", 15L, "Tienda QA");
        when(chatService.crearConversacion(eq("buyer@test.com"), any())).thenReturn(response);

        var result = controller.crearConversacion(request, buyer);

        assertEquals(1L, result.getBody().getId());
        assertEquals("Tienda QA", result.getBody().getVendedorTienda());
    }

    @Test
    void listarConversacionesShouldReturnServicePayload() {
        ChatController controller = new ChatController(chatService);
        UserDetails seller = new User("seller@test.com", "x", Set.of(new SimpleGrantedAuthority("ROLE_VENDEDOR")));
        when(chatService.listarConversacionesPorUsuario("seller@test.com"))
                .thenReturn(List.of(new ConversacionResponse(2L, LocalDateTime.now(), true, "buyer@test.com", 15L, "Tienda QA")));

        var result = controller.listarConversacionesPorUsuario(seller);

        assertEquals(1, result.getBody().size());
        assertEquals(2L, result.getBody().get(0).getId());
    }

    @Test
    void enviarMensajeShouldDelegateContent() {
        ChatController controller = new ChatController(chatService);
        UserDetails seller = new User("seller@test.com", "x", Set.of(new SimpleGrantedAuthority("ROLE_VENDEDOR")));

        MensajeRequest request = new MensajeRequest();
        request.setContenido("Hola desde QA");
        when(chatService.enviarMensaje(eq(9L), eq("seller@test.com"), eq("Hola desde QA")))
                .thenReturn(new MensajeResponse(3L, "Hola desde QA", LocalDateTime.now(), false, "seller@test.com"));

        var result = controller.enviarMensaje(9L, request, seller);

        assertEquals(3L, result.getBody().getId());
        assertEquals("Hola desde QA", result.getBody().getContenido());
    }
}
