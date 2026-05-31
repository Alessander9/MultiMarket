package com.multimarket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

public class PedidoRequest {

    @NotNull(message = "El ID del vendedor/tienda es obligatorio")
    private Long vendedorId;

    @NotNull(message = "El costo de envío es obligatorio")
    @PositiveOrZero(message = "El costo de envío debe ser mayor o igual a cero")
    private BigDecimal costoEnvio;

    @NotEmpty(message = "El pedido debe contener al menos un detalle de producto")
    @Valid
    private List<DetallePedidoRequest> detalles;

    public PedidoRequest() {}

    // Getters y Setters
    public Long getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(Long vendedorId) {
        this.vendedorId = vendedorId;
    }

    public BigDecimal getCostoEnvio() {
        return costoEnvio;
    }

    public void setCostoEnvio(BigDecimal costoEnvio) {
        this.costoEnvio = costoEnvio;
    }

    public List<DetallePedidoRequest> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedidoRequest> detalles) {
        this.detalles = detalles;
    }
}
