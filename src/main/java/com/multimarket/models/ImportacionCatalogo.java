package com.multimarket.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "importaciones_catalogos")
public class ImportacionCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "nombre_archivo", nullable = false, length = 150)
    private String nombreArchivo;

    @NotBlank
    @Column(name = "ruta_archivo", nullable = false, length = 255)
    private String rutaArchivo;

    @NotNull
    @Column(name = "fecha_importacion", nullable = false)
    private LocalDateTime fechaImportacion;

    @NotNull
    @Column(name = "total_registros", nullable = false)
    private Integer totalRegistros = 0;

    @NotNull
    @Column(name = "registros_correctos", nullable = false)
    private Integer registrosCorrectos = 0;

    @NotNull
    @Column(name = "registros_error", nullable = false)
    private Integer registrosError = 0;

    public ImportacionCatalogo() {}

    @PrePersist
    protected void onCreate() {
        this.fechaImportacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public LocalDateTime getFechaImportacion() {
        return fechaImportacion;
    }

    public void setFechaImportacion(LocalDateTime fechaImportacion) {
        this.fechaImportacion = fechaImportacion;
    }

    public Integer getTotalRegistros() {
        return totalRegistros;
    }

    public void setTotalRegistros(Integer totalRegistros) {
        this.totalRegistros = totalRegistros;
    }

    public Integer getRegistrosCorrectos() {
        return registrosCorrectos;
    }

    public void setRegistrosCorrectos(Integer registrosCorrectos) {
        this.registrosCorrectos = registrosCorrectos;
    }

    public Integer getRegistrosError() {
        return registrosError;
    }

    public void setRegistrosError(Integer registrosError) {
        this.registrosError = registrosError;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportacionCatalogo that = (ImportacionCatalogo) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
