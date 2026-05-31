package com.multimarket.controllers;

import com.multimarket.dto.ConversacionRequest;
import com.multimarket.dto.ConversacionResponse;
import com.multimarket.dto.MensajeRequest;
import com.multimarket.dto.MensajeResponse;
import com.multimarket.services.Interfaces.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/conversaciones")
    public ResponseEntity<ConversacionResponse> crearConversacion(
            @Valid @RequestBody ConversacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ConversacionResponse response = chatService.crearConversacion(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversaciones")
    public ResponseEntity<List<ConversacionResponse>> listarConversacionesPorUsuario(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ConversacionResponse> response = chatService.listarConversacionesPorUsuario(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversaciones/{id}/mensajes")
    public ResponseEntity<List<MensajeResponse>> obtenerHistorial(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<MensajeResponse> response = chatService.obtenerHistorial(id, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/conversaciones/{id}/mensajes")
    public ResponseEntity<MensajeResponse> enviarMensaje(
            @PathVariable Long id,
            @Valid @RequestBody MensajeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        MensajeResponse response = chatService.enviarMensaje(id, userDetails.getUsername(), request.getContenido());
        return ResponseEntity.ok(response);
    }
}
