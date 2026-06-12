package com.multimarket.dto;

import java.util.List;

public class RoleResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private List<String> permisos;

    public RoleResponse() {}

    public RoleResponse(Long id, String nombre, String descripcion, List<String> permisos) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.permisos = permisos;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public List<String> getPermisos() { return permisos; }
    public void setPermisos(List<String> permisos) { this.permisos = permisos; }
}
