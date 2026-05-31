package com.multimarket.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ProductoResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private String sku;
    private BigDecimal precio;
    private Integer stock;
    private BigDecimal peso;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Long categoriaId;
    private String categoriaNombre;
    private Long vendedorId;
    private String tiendaNombre;
    private List<ImagenProductoResponse> imagenes;

    public ProductoResponse() {}

    public ProductoResponse(Long id, String nombre, String descripcion, String sku, BigDecimal precio,
                            Integer stock, BigDecimal peso, Boolean activo, LocalDateTime fechaCreacion,
                            LocalDateTime fechaActualizacion, Long categoriaId, String categoriaNombre,
                            Long vendedorId, String tiendaNombre, List<ImagenProductoResponse> imagenes) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.sku = sku;
        this.precio = precio;
        this.stock = stock;
        this.peso = peso;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.categoriaId = categoriaId;
        this.categoriaNombre = categoriaNombre;
        this.vendedorId = vendedorId;
        this.tiendaNombre = tiendaNombre;
        this.imagenes = imagenes;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public BigDecimal getPeso() {
        return peso;
    }

    public void setPeso(BigDecimal peso) {
        this.peso = peso;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public Long getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(Long vendedorId) {
        this.vendedorId = vendedorId;
    }

    public String getTiendaNombre() {
        return tiendaNombre;
    }

    public void setTiendaNombre(String tiendaNombre) {
        this.tiendaNombre = tiendaNombre;
    }

    public List<ImagenProductoResponse> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<ImagenProductoResponse> imagenes) {
        this.imagenes = imagenes;
    }
}
