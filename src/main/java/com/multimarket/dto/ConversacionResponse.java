package com.multimarket.dto;

import java.time.LocalDateTime;

public class ConversacionResponse {
    private Long id;
    private LocalDateTime fechaCreacion;
    private Boolean activa;
    private String compradorCorreo;
    private String vendedorTienda;

    public ConversacionResponse() {}

    public ConversacionResponse(Long id, LocalDateTime fechaCreacion, Boolean activa, String compradorCorreo, String vendedorTienda) {
        this.id = id;
        this.fechaCreacion = fechaCreacion;
        this.activa = activa;
        this.compradorCorreo = compradorCorreo;
        this.vendedorTienda = vendedorTienda;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public String getCompradorCorreo() {
        return compradorCorreo;
    }

    public void setCompradorCorreo(String compradorCorreo) {
        this.compradorCorreo = compradorCorreo;
    }

    public String getVendedorTienda() {
        return vendedorTienda;
    }

    public void setVendedorTienda(String vendedorTienda) {
        this.vendedorTienda = vendedorTienda;
    }
}
