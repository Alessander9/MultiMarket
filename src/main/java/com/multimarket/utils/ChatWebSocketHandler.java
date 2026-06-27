package com.multimarket.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multimarket.dto.MensajeResponse;
import com.multimarket.repositories.ConversacionRepository;
import com.multimarket.services.Interfaces.ChatService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = Logger.getLogger(ChatWebSocketHandler.class.getName());
    
    private final ChatService chatService;
    private final ConversacionRepository conversacionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // Active websocket sessions indexed by user email
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(ChatService chatService, 
                                 ConversacionRepository conversacionRepository) {
        this.chatService = chatService;
        this.conversacionRepository = conversacionRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String email = getEmailParam(session);
        if (email != null && !email.trim().isEmpty()) {
            activeSessions.put(email, session);
            LOGGER.log(Level.INFO, "[WEBSOCKET] Usuario conectado y registrado: {0}", email);
            session.sendMessage(new TextMessage("{\"type\":\"CONNECT\",\"status\":\"OK\",\"message\":\"Conectado como " + email + "\"}"));
        } else {
            LOGGER.log(Level.WARNING, "[WEBSOCKET] Conexión rechazada: email no provisto en query params.");
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload().trim();
        if (payload.isEmpty()) return;

        LOGGER.log(Level.FINE, "[WEBSOCKET] Mensaje recibido: {0}", payload);

        try {
            // Parse client message request
            ChatWsMessage request = objectMapper.readValue(payload, ChatWsMessage.class);
            String senderEmail = getEmailParam(session);

            if (senderEmail == null || !senderEmail.equals(request.getRemitenteCorreo())) {
                sendError(session, "No puedes enviar mensajes a nombre de otro usuario.");
                return;
            }

            // Save and persist message in database
            MensajeResponse savedMsg = chatService.enviarMensaje(
                    request.getConversacionId(), 
                    request.getRemitenteCorreo(), 
                    request.getContenido()
            );

            // Send confirmation back to the sender
            String senderConfirmation = objectMapper.writeValueAsString(new ServerResponse("MESSAGE_ACK", request.getConversacionId(), savedMsg));
            session.sendMessage(new TextMessage(senderConfirmation));

            String recipientEmail = conversacionRepository
                    .findRecipientEmailByConversationIdAndSenderEmail(request.getConversacionId(), senderEmail)
                    .orElse(null);

            if (recipientEmail != null) {
                // If recipient is online, send message in real-time
                WebSocketSession recipientSession = activeSessions.get(recipientEmail);
                if (recipientSession != null && recipientSession.isOpen()) {
                    String forwardPayload = objectMapper.writeValueAsString(new ServerResponse("NEW_MESSAGE", request.getConversacionId(), savedMsg));
                    recipientSession.sendMessage(new TextMessage(forwardPayload));
                    LOGGER.log(Level.INFO, "[WEBSOCKET] Mensaje de {0} reenviado a {1}", new Object[]{senderEmail, recipientEmail});
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[WEBSOCKET] Error procesando mensaje recibido: " + e.getMessage(), e);
            sendError(session, "Error al procesar el mensaje: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String email = getEmailParam(session);
        if (email != null) {
            activeSessions.remove(email);
            LOGGER.log(Level.INFO, "[WEBSOCKET] Usuario desconectado: {0}", email);
        }
    }

    private String getEmailParam(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri != null && uri.getQuery() != null) {
            String[] queryParams = uri.getQuery().split("&");
            for (String param : queryParams) {
                String[] pair = param.split("=");
                if (pair.length > 1 && pair[0].equalsIgnoreCase("email")) {
                    return pair[1];
                }
            }
        }
        return null;
    }

    private void sendError(WebSocketSession session, String errorMessage) throws IOException {
        session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"message\":\"" + errorMessage + "\"}"));
    }

    // Helper model classes for JSON mapping
    public static class ChatWsMessage {
        private Long conversacionId;
        private String remitenteCorreo;
        private String contenido;

        public ChatWsMessage() {}

        public Long getConversacionId() { return conversacionId; }
        public void setConversacionId(Long conversacionId) { this.conversacionId = conversacionId; }
        public String getRemitenteCorreo() { return remitenteCorreo; }
        public void setRemitenteCorreo(String remitenteCorreo) { this.remitenteCorreo = remitenteCorreo; }
        public String getContenido() { return contenido; }
        public void setContenido(String contenido) { this.contenido = contenido; }
    }

    public static class ServerResponse {
        private String type;
        private Long conversacionId;
        private MensajeResponse data;

        public ServerResponse(String type, Long conversacionId, MensajeResponse data) {
            this.type = type;
            this.conversacionId = conversacionId;
            this.data = data;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Long getConversacionId() { return conversacionId; }
        public void setConversacionId(Long conversacionId) { this.conversacionId = conversacionId; }
        public MensajeResponse getData() { return data; }
        public void setData(MensajeResponse data) { this.data = data; }
    }
}
