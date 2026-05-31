package com.multimarket.kafka.events;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PedidoCreadoEvent implements Serializable {
    private Long pedidoId;
    private String numeroPedido;
    private String compradorCorreo;
    private BigDecimal total;
    private LocalDateTime fechaPedido;

    public PedidoCreadoEvent() {}

    public PedidoCreadoEvent(Long pedidoId, String numeroPedido, String compradorCorreo, BigDecimal total) {
        this.pedidoId = pedidoId;
        this.numeroPedido = numeroPedido;
        this.compradorCorreo = compradorCorreo;
        this.total = total;
        this.fechaPedido = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public String getCompradorCorreo() {
        return compradorCorreo;
    }

    public void setCompradorCorreo(String compradorCorreo) {
        this.compradorCorreo = compradorCorreo;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }
}
