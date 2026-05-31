package com.multimarket.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VendedorResponse {

    private Long id;
    private Long usuarioId;
    private String nombreTienda;
    private String descripcion;
    private String region;
    private String direccion;
    private String logo;
    private String banner;
    private LocalDateTime fechaCreacion;
    private Boolean activo;
    private BigDecimal calificacionPromedio;

    public VendedorResponse() {}

    public VendedorResponse(Long id, Long usuarioId, String nombreTienda, String descripcion, String region,
                            String direccion, String logo, String banner, LocalDateTime fechaCreacion,
                            Boolean activo, BigDecimal calificacionPromedio) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nombreTienda = nombreTienda;
        this.descripcion = descripcion;
        this.region = region;
        this.direccion = direccion;
        this.logo = logo;
        this.banner = banner;
        this.fechaCreacion = fechaCreacion;
        this.activo = activo;
        this.calificacionPromedio = calificacionPromedio;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombreTienda() {
        return nombreTienda;
    }

    public void setNombreTienda(String nombreTienda) {
        this.nombreTienda = nombreTienda;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public BigDecimal getCalificacionPromedio() {
        return calificacionPromedio;
    }

    public void setCalificacionPromedio(BigDecimal calificacionPromedio) {
        this.calificacionPromedio = calificacionPromedio;
    }
}
