package com.multimarket.services.Interfaces;

import com.multimarket.models.Notificacion;
import com.multimarket.models.TipoNotificacion;
import java.util.List;

public interface NotificacionService {
    Notificacion generarNotificacion(Long usuarioId, String titulo, String mensaje, TipoNotificacion tipo);
    void marcarComoLeida(Long id, String usuarioCorreo);
    List<Notificacion> consultarHistorial(String usuarioCorreo);
}
