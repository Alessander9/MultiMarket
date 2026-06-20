package com.multimarket.services.impl;

import com.multimarket.models.Notificacion;
import com.multimarket.models.TipoNotificacion;
import com.multimarket.models.Usuario;
import com.multimarket.dto.NotificacionResponse;
import com.multimarket.repositories.NotificacionRepository;
import com.multimarket.repositories.UsuarioRepository;
import com.multimarket.services.Interfaces.NotificacionService;
import com.multimarket.services.Interfaces.NotificationRealtimeService;
import com.multimarket.kafka.KafkaProducer;
import com.multimarket.kafka.events.NotificacionGeneradaEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final KafkaProducer kafkaProducer;
    private final NotificationRealtimeService notificationRealtimeService;

    public NotificacionServiceImpl(NotificacionRepository notificacionRepository,
                                   UsuarioRepository usuarioRepository,
                                   KafkaProducer kafkaProducer,
                                   NotificationRealtimeService notificationRealtimeService) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.kafkaProducer = kafkaProducer;
        this.notificationRealtimeService = notificationRealtimeService;
    }

    @Override
    public Notificacion generarNotificacion(Long usuarioId, String titulo, String mensaje, TipoNotificacion tipo) {
        Usuario usr = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario con ID: " + usuarioId));

        Notificacion notif = new Notificacion();
        notif.setUsuario(usr);
        notif.setTitulo(titulo);
        notif.setMensaje(mensaje);
        notif.setTipo(tipo);
        notif.setLeida(false);
        notif.setFechaCreacion(LocalDateTime.now());

        Notificacion savedNotif = notificacionRepository.save(notif);

        // Publicar evento en Kafka para notificaciones en tiempo real o logs asíncronos
        kafkaProducer.sendNotificacionEvent(new NotificacionGeneradaEvent(
                usuarioId,
                titulo,
                mensaje,
                tipo.name()
        ));

        publishRealtimeNotification(savedNotif);

        return savedNotif;
    }

    @Async
    public void publishRealtimeNotification(Notificacion notif) {
        notificationRealtimeService.broadcastNewNotification(new NotificacionResponse(
                notif.getId(),
                notif.getTitulo(),
                notif.getMensaje(),
                notif.getTipo().name(),
                notif.getLeida(),
                notif.getFechaCreacion(),
                notif.getUsuario() != null ? notif.getUsuario().getCorreo() : null
        ));
    }

    @Override
    public void marcarComoLeida(Long id, String usuarioCorreo) {
        Notificacion notif = notificacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la notificación con ID: " + id));

        // Validación de seguridad: el usuario logueado debe ser el dueño de la notificación
        if (!notif.getUsuario().getCorreo().equals(usuarioCorreo)) {
            throw new SecurityException("Acceso Denegado: No puedes modificar notificaciones ajenas.");
        }

        notif.setLeida(true);
        notificacionRepository.save(notif);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionResponse> consultarHistorial(String usuarioCorreo) {
        return notificacionRepository.findByUsuarioCorreoOrderByFechaCreacionDesc(usuarioCorreo).stream()
                .map(notif -> new NotificacionResponse(
                        notif.getId(),
                        notif.getTitulo(),
                        notif.getMensaje(),
                        notif.getTipo().name(),
                        notif.getLeida(),
                        notif.getFechaCreacion(),
                        notif.getUsuario() != null ? notif.getUsuario().getCorreo() : null
                ))
                .toList();
    }
}
