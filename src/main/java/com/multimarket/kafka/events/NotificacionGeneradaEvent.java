package com.multimarket.kafka.events;

import java.io.Serializable;
import java.time.LocalDateTime;

public class NotificacionGeneradaEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long usuarioId;
    private String titulo;
    private String mensaje;
    private String tipo;
    private LocalDateTime fechaCreacion;

    public NotificacionGeneradaEvent() {}

    public NotificacionGeneradaEvent(Long usuarioId, String titulo, String mensaje, String tipo) {
        this.usuarioId = usuarioId;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
