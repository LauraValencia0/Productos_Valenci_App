package com.valenci.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Factura {
    private int idFactura;
    private Pedido pedido;
    private LocalDateTime fechaFactura;
    private BigDecimal totalFactura;
    private BigDecimal iva;

    public Factura(int idFactura, Pedido pedido, LocalDateTime fechaFactura, BigDecimal totalFactura, BigDecimal iva) {
        this.idFactura = idFactura;
        this.pedido = pedido;
        this.fechaFactura = fechaFactura;
        this.totalFactura = totalFactura;
        this.iva = iva;
    }

    public Factura() {
    }

    public int getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(int idFactura) {
        this.idFactura = idFactura;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public LocalDateTime getFechaFactura() {
        return fechaFactura;
    }

    public void setFechaFactura(LocalDateTime fechaFactura) {
        this.fechaFactura = fechaFactura;
    }

    public BigDecimal getTotalFactura() {
        return totalFactura;
    }

    public void setTotalFactura(BigDecimal totalFactura) {
        this.totalFactura = totalFactura;
    }

    public BigDecimal getIva() {
        return iva;
    }

    public void setIva(BigDecimal iva) {
        this.iva = iva;
    }
}
