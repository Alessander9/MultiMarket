package com.multimarket.dto;

import jakarta.validation.constraints.NotNull;

public class ConversacionRequest {

    @NotNull(message = "El ID del vendedor/tienda es obligatorio")
    private Long vendedorId;

    public ConversacionRequest() {}

    public Long getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(Long vendedorId) {
        this.vendedorId = vendedorId;
    }
}
