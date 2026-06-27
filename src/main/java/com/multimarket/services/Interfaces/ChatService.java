package com.multimarket.services.Interfaces;

import com.multimarket.dto.ConversacionRequest;
import com.multimarket.dto.ConversacionResponse;
import com.multimarket.dto.MensajeResponse;
import java.util.List;

public interface ChatService {
    ConversacionResponse crearConversacion(String compradorCorreo, ConversacionRequest request);
    List<ConversacionResponse> listarConversacionesPorUsuario(String usuarioCorreo);
    List<MensajeResponse> obtenerHistorial(Long conversacionId, String usuarioCorreo);
    
    // Core transactional API used by both REST endpoints and real-time Sockets
    MensajeResponse enviarMensaje(Long conversacionId, String remitenteCorreo, String contenido);
}
