package com.multimarket.dto;

import java.math.BigDecimal;
import java.util.List;

public class CompraAgrupadaResponse {

    private Long id;
    private String numeroCompra;
    private java.time.LocalDateTime fechaCompra;
    private String metodoPago;
    private String estadoGeneral;
    private List<PedidoResponse> pedidos;
    private BigDecimal subtotal;
    private BigDecimal impuesto;
    private BigDecimal costoEnvioTotal;
    private BigDecimal total;

    public CompraAgrupadaResponse() {}

    public CompraAgrupadaResponse(Long id, String numeroCompra, java.time.LocalDateTime fechaCompra, String metodoPago, String estadoGeneral, List<PedidoResponse> pedidos, BigDecimal subtotal, BigDecimal impuesto, BigDecimal costoEnvioTotal, BigDecimal total) {
        this.id = id;
        this.numeroCompra = numeroCompra;
        this.fechaCompra = fechaCompra;
        this.metodoPago = metodoPago;
        this.estadoGeneral = estadoGeneral;
        this.pedidos = pedidos;
        this.subtotal = subtotal;
        this.impuesto = impuesto;
        this.costoEnvioTotal = costoEnvioTotal;
        this.total = total;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroCompra() {
        return numeroCompra;
    }

    public void setNumeroCompra(String numeroCompra) {
        this.numeroCompra = numeroCompra;
    }

    public java.time.LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(java.time.LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstadoGeneral() {
        return estadoGeneral;
    }

    public void setEstadoGeneral(String estadoGeneral) {
        this.estadoGeneral = estadoGeneral;
    }

    public List<PedidoResponse> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<PedidoResponse> pedidos) {
        this.pedidos = pedidos;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(BigDecimal impuesto) {
        this.impuesto = impuesto;
    }

    public BigDecimal getCostoEnvioTotal() {
        return costoEnvioTotal;
    }

    public void setCostoEnvioTotal(BigDecimal costoEnvioTotal) {
        this.costoEnvioTotal = costoEnvioTotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
