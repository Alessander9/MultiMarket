package com.multimarket.dto;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(max = 200)
    private String nombrePersonal;

    @Size(max = 20)
    private String telefono;

    private String direccion;

    @Size(max = 255)
    private String fotoPerfil;

    public UpdateProfileRequest() {}

    public String getNombrePersonal() {
        return nombrePersonal;
    }

    public void setNombrePersonal(String nombrePersonal) {
        this.nombrePersonal = nombrePersonal;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }
}
