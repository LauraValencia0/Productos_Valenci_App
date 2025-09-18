package com.valenci.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pago {
    private int idPago;
    private Pedido pedido;
    private BigDecimal monto;
    private LocalDateTime fechaPago;
    private MetodoPago metodoPago;

    public Pago(int idPago, Pedido pedido, BigDecimal monto, LocalDateTime fechaPago, MetodoPago metodoPago) {
        this.idPago = idPago;
        this.pedido = pedido;
        this.monto = monto;
        this.fechaPago = fechaPago;
        this.metodoPago = metodoPago;
    }

    public Pago() {
    }

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }
}