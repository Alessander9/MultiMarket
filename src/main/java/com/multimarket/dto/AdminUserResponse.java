package com.multimarket.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AdminUserResponse {

    private Long id;
    private String correo;
    private List<String> roles;
    private Boolean estado;
    private Boolean correoVerificado;
    private LocalDateTime fechaRegistro;
    private Integer intentosFallidos;
    private Boolean bloqueado;
    private String nombres;
    private String apellidos;
    private String dni;
    private String telefono;
    private String direccion;
    private String fotoPerfil;
    private LocalDate fechaNacimiento;

    public AdminUserResponse() {}

    public AdminUserResponse(Long id, String correo, List<String> roles, Boolean estado, Boolean correoVerificado,
                             LocalDateTime fechaRegistro, Integer intentosFallidos, Boolean bloqueado,
                             String nombres, String apellidos, String dni, String telefono, String direccion,
                             String fotoPerfil, LocalDate fechaNacimiento) {
        this.id = id;
        this.correo = correo;
        this.roles = roles;
        this.estado = estado;
        this.correoVerificado = correoVerificado;
        this.fechaRegistro = fechaRegistro;
        this.intentosFallidos = intentosFallidos;
        this.bloqueado = bloqueado;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.dni = dni;
        this.telefono = telefono;
        this.direccion = direccion;
        this.fotoPerfil = fotoPerfil;
        this.fechaNacimiento = fechaNacimiento;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }
    public Boolean getCorreoVerificado() { return correoVerificado; }
    public void setCorreoVerificado(Boolean correoVerificado) { this.correoVerificado = correoVerificado; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public Integer getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(Integer intentosFallidos) { this.intentosFallidos = intentosFallidos; }
    public Boolean getBloqueado() { return bloqueado; }
    public void setBloqueado(Boolean bloqueado) { this.bloqueado = bloqueado; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
}
