package com.multimarket.dto;

import java.time.LocalDateTime;

public class InventarioResponse {
    private Long id;
    private Integer stockActual;
    private Integer stockMinimo;
    private LocalDateTime ultimaActualizacion;
    private Long productoId;
    private String productoNombre;

    public InventarioResponse() {}

    public InventarioResponse(Long id, Integer stockActual, Integer stockMinimo, LocalDateTime ultimaActualizacion, Long productoId, String productoNombre) {
        this.id = id;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.ultimaActualizacion = ultimaActualizacion;
        this.productoId = productoId;
        this.productoNombre = productoNombre;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStockActual() {
        return stockActual;
    }

    public void setStockActual(Integer stockActual) {
        this.stockActual = stockActual;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }
}
