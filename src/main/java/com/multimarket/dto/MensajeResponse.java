package com.multimarket.dto;

import java.time.LocalDateTime;

public class MensajeResponse {
    private Long id;
    private String contenido;
    private LocalDateTime fechaEnvio;
    private Boolean leido;
    private String remitenteCorreo;

    public MensajeResponse() {}

    public MensajeResponse(Long id, String contenido, LocalDateTime fechaEnvio, Boolean leido, String remitenteCorreo) {
        this.id = id;
        this.contenido = contenido;
        this.fechaEnvio = fechaEnvio;
        this.leido = leido;
        this.remitenteCorreo = remitenteCorreo;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public Boolean getLeido() {
        return leido;
    }

    public void setLeido(Boolean leido) {
        this.leido = leido;
    }

    public String getRemitenteCorreo() {
        return remitenteCorreo;
    }

    public void setRemitenteCorreo(String remitenteCorreo) {
        this.remitenteCorreo = remitenteCorreo;
    }
}
