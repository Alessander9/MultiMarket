package com.multimarket.dto;

import com.multimarket.models.MetodoPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class CompraAgrupadaPagoRequest {

    @NotEmpty(message = "La compra debe incluir al menos un grupo de pedido")
    @Valid
    private List<GrupoPedidoRequest> grupos;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    @Size(max = 19)
    private String numeroTarjeta;

    @Size(max = 4)
    private String cvv;

    @Size(max = 7)
    private String fechaExpiracion;

    public List<GrupoPedidoRequest> getGrupos() {
        return grupos;
    }

    public void setGrupos(List<GrupoPedidoRequest> grupos) {
        this.grupos = grupos;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(String fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }
}
