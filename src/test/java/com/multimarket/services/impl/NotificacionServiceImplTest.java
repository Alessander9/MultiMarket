package com.multimarket.services.impl;

import com.multimarket.dto.NotificacionResponse;
import com.multimarket.kafka.KafkaProducer;
import com.multimarket.models.Notificacion;
import com.multimarket.models.TipoNotificacion;
import com.multimarket.models.Usuario;
import com.multimarket.repositories.NotificacionRepository;
import com.multimarket.repositories.UsuarioRepository;
import com.multimarket.services.Interfaces.NotificationRealtimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceImplTest {

    @Mock private NotificacionRepository notificacionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private KafkaProducer kafkaProducer;
    @Mock private NotificationRealtimeService notificationRealtimeService;

    @InjectMocks
    private NotificacionServiceImpl service;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setCorreo("buyer@test.com");
    }

    @Test
    void generarNotificacionShouldPersistAndBroadcast() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(inv -> {
            Notificacion n = inv.getArgument(0);
            n.setId(99L);
            return n;
        });
        doNothing().when(kafkaProducer).sendNotificacionEvent(any());

        Notificacion saved = service.generarNotificacion(1L, "Nueva orden", "Tu pedido fue aprobado", TipoNotificacion.SISTEMA);

        assertEquals("Nueva orden", saved.getTitulo());
        assertFalse(saved.getLeida());
        verify(kafkaProducer).sendNotificacionEvent(any());
        verify(notificationRealtimeService).broadcastNewNotification(any(NotificacionResponse.class));
    }

    @Test
    void marcarComoLeidaShouldRejectForeignNotification() {
        Notificacion notif = new Notificacion();
        notif.setId(50L);
        notif.setUsuario(usuario);
        notif.setTitulo("A");
        notif.setMensaje("B");
        notif.setTipo(TipoNotificacion.SISTEMA);
        notif.setLeida(false);

        Usuario other = new Usuario();
        other.setId(2L);
        other.setCorreo("other@test.com");

        when(notificacionRepository.findById(50L)).thenReturn(Optional.of(notif));

        SecurityException ex = assertThrows(SecurityException.class,
                () -> service.marcarComoLeida(50L, other.getCorreo()));

        assertTrue(ex.getMessage().contains("Acceso Denegado"));
    }

    @Test
    void consultarHistorialShouldMapResponses() {
        Notificacion notif = new Notificacion();
        notif.setId(3L);
        notif.setUsuario(usuario);
        notif.setTitulo("Hola");
        notif.setMensaje("Mensaje");
        notif.setTipo(TipoNotificacion.SISTEMA);
        notif.setLeida(true);

        when(notificacionRepository.findByUsuarioCorreoOrderByFechaCreacionDesc("buyer@test.com"))
                .thenReturn(List.of(notif));

        var result = service.consultarHistorial("buyer@test.com");

        assertEquals(1, result.size());
        assertEquals("Hola", result.get(0).getTitulo());
        assertEquals("buyer@test.com", result.get(0).getUsuarioCorreo());
    }
}
