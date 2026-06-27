package com.multimarket.utils;

import com.multimarket.dto.MensajeResponse;
import com.multimarket.models.Conversacion;
import com.multimarket.models.Mensaje;
import com.multimarket.models.Usuario;
import com.multimarket.models.Vendedor;
import com.multimarket.repositories.ConversacionRepository;
import com.multimarket.services.Interfaces.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ChatWebSocketHandlerTest {

    @Test
    void connectionWithoutEmailShouldCloseSession() throws Exception {
        ChatService chatService = mock(ChatService.class);
        ConversacionRepository repository = mock(ConversacionRepository.class);
        ChatWebSocketHandler handler = new ChatWebSocketHandler(chatService, repository);
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getUri()).thenReturn(new URI("ws://localhost/chat-websocket"));

        handler.afterConnectionEstablished(session);

        verify(session).close(CloseStatus.BAD_DATA);
    }

    @Test
    void handleTextMessageShouldSendAckAndForwardToRecipient() throws Exception {
        ChatService chatService = mock(ChatService.class);
        ConversacionRepository repository = mock(ConversacionRepository.class);
        ChatWebSocketHandler handler = new ChatWebSocketHandler(chatService, repository);

        Usuario comprador = new Usuario();
        comprador.setCorreo("buyer@test.com");
        Usuario sellerUser = new Usuario();
        sellerUser.setCorreo("seller@test.com");
        Vendedor vendedor = new Vendedor();
        vendedor.setUsuario(sellerUser);

        Conversacion conversacion = new Conversacion();
        conversacion.setId(11L);
        conversacion.setComprador(comprador);
        conversacion.setVendedor(vendedor);
        conversacion.setActiva(true);

        WebSocketSession sender = mock(WebSocketSession.class);
        WebSocketSession recipient = mock(WebSocketSession.class);
        when(sender.getUri()).thenReturn(new URI("ws://localhost/chat-websocket?email=buyer@test.com"));
        when(sender.isOpen()).thenReturn(true);
        when(recipient.isOpen()).thenReturn(true);

        when(chatService.enviarMensaje(11L, "buyer@test.com", "Hola chat"))
                .thenReturn(new MensajeResponse(1L, "Hola chat", LocalDateTime.now(), false, "buyer@test.com"));
        when(repository.findById(11L)).thenReturn(Optional.of(conversacion));
        when(repository.findRecipientEmailByConversationIdAndSenderEmail(11L, "buyer@test.com"))
                .thenReturn(Optional.of("seller@test.com"));

        handler.afterConnectionEstablished(sender);
        var recipientField = ChatWebSocketHandler.class.getDeclaredField("activeSessions");
        recipientField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, WebSocketSession> active = (java.util.Map<String, WebSocketSession>) recipientField.get(handler);
        active.put("seller@test.com", recipient);

        handler.handleTextMessage(sender, new TextMessage("{\"conversacionId\":11,\"remitenteCorreo\":\"buyer@test.com\",\"contenido\":\"Hola chat\"}"));

        verify(sender, atLeastOnce()).sendMessage(any(TextMessage.class));
        verify(recipient, atLeastOnce()).sendMessage(any(TextMessage.class));
        verify(chatService).enviarMensaje(11L, "buyer@test.com", "Hola chat");
    }
}
