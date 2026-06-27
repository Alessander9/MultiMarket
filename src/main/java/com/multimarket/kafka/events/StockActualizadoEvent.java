package com.multimarket.kafka.events;

import java.io.Serializable;
import java.time.LocalDateTime;

public class StockActualizadoEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productoId;
    private Integer stockAnterior;
    private Integer stockNuevo;
    private String tipoMovimiento;
    private Integer cantidad;
    private LocalDateTime fechaActualizacion;

    public StockActualizadoEvent() {}

    public StockActualizadoEvent(Long productoId, Integer stockAnterior, Integer stockNuevo, String tipoMovimiento, Integer cantidad) {
        this.productoId = productoId;
        this.stockAnterior = stockAnterior;
        this.stockNuevo = stockNuevo;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Integer getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(Integer stockAnterior) {
        this.stockAnterior = stockAnterior;
    }

    public Integer getStockNuevo() {
        return stockNuevo;
    }

    public void setStockNuevo(Integer stockNuevo) {
        this.stockNuevo = stockNuevo;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
