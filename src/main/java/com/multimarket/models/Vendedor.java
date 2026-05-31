package com.multimarket.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "vendedores")
public class Vendedor extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nombre_tienda", nullable = false, unique = true, length = 100)
    private String nombreTienda;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String region;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String direccion;

    @Size(max = 255)
    @Column(length = 255)
    private String logo;

    @Size(max = 255)
    @Column(length = 255)
    private String banner;

    @NotNull
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @NotNull
    @Column(nullable = false)
    private Boolean activo = true;

    @NotNull
    @Column(name = "calificacion_promedio", nullable = false, precision = 3, scale = 2)
    private BigDecimal calificacionPromedio = BigDecimal.ZERO;

    public Vendedor() {}

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.activo == null) {
            this.activo = true;
        }
        if (this.calificacionPromedio == null) {
            this.calificacionPromedio = BigDecimal.ZERO;
        }
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vendedor vendedor = (Vendedor) o;
        return id != null && id.equals(vendedor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
