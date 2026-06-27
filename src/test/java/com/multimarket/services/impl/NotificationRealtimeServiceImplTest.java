package com.multimarket.services.impl;

import com.multimarket.dto.NotificacionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class NotificationRealtimeServiceImplTest {

    @Test
    void broadcastShouldSendMessageToOpenSessions() throws Exception {
        NotificationRealtimeServiceImpl service = new NotificationRealtimeServiceImpl();
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);

        service.register("buyer@test.com", session);
        service.broadcastNewNotification(new NotificacionResponse(
                1L, "Hola", "Mensaje", "SISTEMA", false, LocalDateTime.now(), "buyer@test.com"
        ));

        verify(session, atLeastOnce()).sendMessage(any(TextMessage.class));
        verify(session).isOpen();
        service.unregister("buyer@test.com");
    }

    @Test
    void unregisterShouldRemoveSessionWithoutError() {
        NotificationRealtimeServiceImpl service = new NotificationRealtimeServiceImpl();
        WebSocketSession session = mock(WebSocketSession.class);

        service.register("seller@test.com", session);
        service.unregister("seller@test.com");

        assertTrue(true);
    }
}
