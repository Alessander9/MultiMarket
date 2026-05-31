package com.multimarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MensajeRequest {

    @NotBlank(message = "El contenido del mensaje no puede estar vacío")
    @Size(max = 1000, message = "El mensaje no puede exceder los 1000 caracteres")
    private String contenido;

    public MensajeRequest() {}

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
}
