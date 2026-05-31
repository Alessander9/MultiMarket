package com.multimarket.dto;

import com.multimarket.models.MetodoPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PagoRequest {

    @NotNull(message = "El ID del pedido es obligatorio")
    private Long pedidoId;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    // Campos opcionales para la simulación de validación de tarjetas
    @Size(max = 19)
    private String numeroTarjeta;

    @Size(max = 4)
    private String cvv;

    @Size(max = 7)
    private String fechaExpiracion;

    public PagoRequest() {}

    // Getters y Setters
    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
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
