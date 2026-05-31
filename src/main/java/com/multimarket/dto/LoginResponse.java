package com.multimarket.dto;

import java.util.Set;

public class LoginResponse {

    private String token;
    private String correo;
    private Set<String> roles;

    public LoginResponse() {}

    public LoginResponse(String token, String correo, Set<String> roles) {
        this.token = token;
        this.correo = correo;
        this.roles = roles;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
