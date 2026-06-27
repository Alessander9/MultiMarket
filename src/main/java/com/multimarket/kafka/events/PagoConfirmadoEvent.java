package com.multimarket.kafka.events;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagoConfirmadoEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long pagoId;
    private Long pedidoId;
    private BigDecimal monto;
    private String metodoPago;
    private String codigoOperacion;
    private LocalDateTime fechaPago;

    public PagoConfirmadoEvent() {}

    public PagoConfirmadoEvent(Long pagoId, Long pedidoId, BigDecimal monto, String metodoPago, String codigoOperacion) {
        this.pagoId = pagoId;
        this.pedidoId = pedidoId;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.codigoOperacion = codigoOperacion;
        this.fechaPago = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getPagoId() {
        return pagoId;
    }

    public void setPagoId(Long pagoId) {
        this.pagoId = pagoId;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getCodigoOperacion() {
        return codigoOperacion;
    }

    public void setCodigoOperacion(String codigoOperacion) {
        this.codigoOperacion = codigoOperacion;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }
}
