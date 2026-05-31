package com.multimarket.dto;

import com.multimarket.models.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public class ActualizarEstadoPedidoRequest {

    @NotNull(message = "El nuevo estado del pedido es obligatorio")
    private EstadoPedido estado;

    public ActualizarEstadoPedidoRequest() {}

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }
}
