package com.multimarket.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PedidoResponse {
    private Long id;
    private String numeroPedido;
    private LocalDateTime fechaPedido;
    private BigDecimal subtotal;
    private BigDecimal impuesto;
    private BigDecimal costoEnvio;
    private BigDecimal total;
    private String estado;
    private String compradorCorreo;
    private Long vendedorId;
    private String vendedorTienda;
    private List<DetallePedidoResponse> detalles;

    public PedidoResponse() {}

    public PedidoResponse(Long id, String numeroPedido, LocalDateTime fechaPedido, BigDecimal subtotal, BigDecimal impuesto, BigDecimal costoEnvio, BigDecimal total, String estado, String compradorCorreo, Long vendedorId, String vendedorTienda, List<DetallePedidoResponse> detalles) {
        this.id = id;
        this.numeroPedido = numeroPedido;
        this.fechaPedido = fechaPedido;
        this.subtotal = subtotal;
        this.impuesto = impuesto;
        this.costoEnvio = costoEnvio;
        this.total = total;
        this.estado = estado;
        this.compradorCorreo = compradorCorreo;
        this.vendedorId = vendedorId;
        this.vendedorTienda = vendedorTienda;
        this.detalles = detalles;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
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

    public BigDecimal getCostoEnvio() {
        return costoEnvio;
    }

    public void setCostoEnvio(BigDecimal costoEnvio) {
        this.costoEnvio = costoEnvio;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCompradorCorreo() {
        return compradorCorreo;
    }

    public void setCompradorCorreo(String compradorCorreo) {
        this.compradorCorreo = compradorCorreo;
    }

    public Long getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(Long vendedorId) {
        this.vendedorId = vendedorId;
    }

    public String getVendedorTienda() {
        return vendedorTienda;
    }

    public void setVendedorTienda(String vendedorTienda) {
        this.vendedorTienda = vendedorTienda;
    }

    public List<DetallePedidoResponse> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedidoResponse> detalles) {
        this.detalles = detalles;
    }
}
