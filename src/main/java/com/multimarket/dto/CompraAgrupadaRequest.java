package com.multimarket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CompraAgrupadaRequest {

    @NotEmpty(message = "La compra debe incluir al menos un grupo de pedido")
    @Valid
    private List<GrupoPedidoRequest> grupos;

    public List<GrupoPedidoRequest> getGrupos() {
        return grupos;
    }

    public void setGrupos(List<GrupoPedidoRequest> grupos) {
        this.grupos = grupos;
    }
}
