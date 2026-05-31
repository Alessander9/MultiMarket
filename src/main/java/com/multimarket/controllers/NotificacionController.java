package com.multimarket.controllers;

import com.multimarket.models.Notificacion;
import com.multimarket.models.TipoNotificacion;
import com.multimarket.services.Interfaces.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    public ResponseEntity<List<Notificacion>> getHistorial(@AuthenticationPrincipal UserDetails userDetails) {
        List<Notificacion> result = notificacionService.consultarHistorial(userDetails.getUsername());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/leer")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable("id") Long id,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        notificacionService.marcarComoLeida(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test")
    public ResponseEntity<Notificacion> createTestNotification(@RequestParam("usuarioId") Long usuarioId,
                                                               @RequestParam("titulo") String titulo,
                                                               @RequestParam("mensaje") String mensaje,
                                                               @RequestParam("tipo") TipoNotificacion tipo) {
        Notificacion result = notificacionService.generarNotificacion(usuarioId, titulo, mensaje, tipo);
        return ResponseEntity.ok(result);
    }
}
