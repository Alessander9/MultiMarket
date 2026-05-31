package com.multimarket.dto;

import java.time.LocalDateTime;

public class MovimientoInventarioResponse {
    private Long id;
    private String tipoMovimiento;
    private Integer cantidad;
    private String observacion;
    private LocalDateTime fechaMovimiento;

    public MovimientoInventarioResponse() {}

    public MovimientoInventarioResponse(Long id, String tipoMovimiento, Integer cantidad, String observacion, LocalDateTime fechaMovimiento) {
        this.id = id;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.observacion = observacion;
        this.fechaMovimiento = fechaMovimiento;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(LocalDateTime fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }
}
