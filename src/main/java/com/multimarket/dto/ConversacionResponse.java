package com.multimarket.dto;

import java.time.LocalDateTime;

public class ConversacionResponse {
    private Long id;
    private LocalDateTime fechaCreacion;
    private Boolean activa;
    private String compradorCorreo;
    private Long vendedorId;
    private String vendedorTienda;
    private String ultimoMensaje;
    private LocalDateTime fechaUltimoMensaje;
    private Integer noLeidos;

    public ConversacionResponse() {}

    public ConversacionResponse(Long id, LocalDateTime fechaCreacion, Boolean activa, String compradorCorreo, Long vendedorId, String vendedorTienda) {
        this.id = id;
        this.fechaCreacion = fechaCreacion;
        this.activa = activa;
        this.compradorCorreo = compradorCorreo;
        this.vendedorId = vendedorId;
        this.vendedorTienda = vendedorTienda;
    }

    public ConversacionResponse(Long id, LocalDateTime fechaCreacion, Boolean activa, String compradorCorreo, Long vendedorId, String vendedorTienda, String ultimoMensaje, LocalDateTime fechaUltimoMensaje, Integer noLeidos) {
        this(id, fechaCreacion, activa, compradorCorreo, vendedorId, vendedorTienda);
        this.ultimoMensaje = ultimoMensaje;
        this.fechaUltimoMensaje = fechaUltimoMensaje;
        this.noLeidos = noLeidos;
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

    public Long getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(Long vendedorId) {
        this.vendedorId = vendedorId;
    }

    public String getVendedorTienda() {
        return vendedorTienda;
    }

    public void setVendedorTienda(String vendedorTienda) {
        this.vendedorTienda = vendedorTienda;
    }

    public String getUltimoMensaje() {
        return ultimoMensaje;
    }

    public void setUltimoMensaje(String ultimoMensaje) {
        this.ultimoMensaje = ultimoMensaje;
    }

    public LocalDateTime getFechaUltimoMensaje() {
        return fechaUltimoMensaje;
    }

    public void setFechaUltimoMensaje(LocalDateTime fechaUltimoMensaje) {
        this.fechaUltimoMensaje = fechaUltimoMensaje;
    }

    public Integer getNoLeidos() {
        return noLeidos;
    }

    public void setNoLeidos(Integer noLeidos) {
        this.noLeidos = noLeidos;
    }
}
