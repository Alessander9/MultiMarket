package com.multimarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VendedorRequest {

    @NotBlank(message = "El nombre de la tienda es obligatorio")
    @Size(max = 100)
    private String nombreTienda;

    private String descripcion;

    @NotBlank(message = "La región es obligatoria")
    @Size(max = 100)
    private String region;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    private String logo;

    private String banner;

    private String correoUsuario;

    public VendedorRequest() {}

    // Getters y Setters
    public String getCorreoUsuario() {
        return correoUsuario;
    }

    public void setCorreoUsuario(String correoUsuario) {
        this.correoUsuario = correoUsuario;
    }

    // Getters y Setters
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
}
