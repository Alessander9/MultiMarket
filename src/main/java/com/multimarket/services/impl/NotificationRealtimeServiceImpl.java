package com.multimarket.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multimarket.dto.NotificacionResponse;
import com.multimarket.services.Interfaces.NotificationRealtimeService;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationRealtimeServiceImpl implements NotificationRealtimeService {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public void register(String email, WebSocketSession session) {
        if (email != null && session != null) {
            sessions.put(email, session);
        }
    }

    public void unregister(String email) {
        if (email != null) {
            sessions.remove(email);
        }
    }

    @Override
    public void broadcastNewNotification(NotificacionResponse notification) {
        sessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(mapper.writeValueAsString(
                            Map.of("type", "NOTIFICATION", "data", notification)
                    )));
                }
            } catch (Exception ignored) {
            }
        });
    }
}
