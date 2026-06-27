package com.multimarket.kafka.events;

import java.io.Serializable;
import java.time.LocalDateTime;

public class UsuarioRegistradoEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long usuarioId;
    private String correo;
    private String nombres;
    private LocalDateTime fechaRegistro;

    public UsuarioRegistradoEvent() {}

    public UsuarioRegistradoEvent(Long usuarioId, String correo, String nombres) {
        this.usuarioId = usuarioId;
        this.correo = correo;
        this.nombres = nombres;
        this.fechaRegistro = LocalDateTime.now();
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
