package com.multimarket.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "exportaciones_catalogos")
public class ExportacionCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FormatoExportacion formato;

    @NotNull
    @Column(name = "fecha_exportacion", nullable = false)
    private LocalDateTime fechaExportacion;

    @Column(name = "ruta_archivo", length = 255)
    private String rutaArchivo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoExportacion estado = EstadoExportacion.PENDIENTE;

    public ExportacionCatalogo() {}

    @PrePersist
    protected void onCreate() {
        this.fechaExportacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoExportacion.PENDIENTE;
        }
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FormatoExportacion getFormato() {
        return formato;
    }

    public void setFormato(FormatoExportacion formato) {
        this.formato = formato;
    }

    public LocalDateTime getFechaExportacion() {
        return fechaExportacion;
    }

    public void setFechaExportacion(LocalDateTime fechaExportacion) {
        this.fechaExportacion = fechaExportacion;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public EstadoExportacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoExportacion estado) {
        this.estado = estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportacionCatalogo that = (ExportacionCatalogo) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
