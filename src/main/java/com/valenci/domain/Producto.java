package com.valenci.domain;

import java.math.BigDecimal;

public class Producto {

    private int idProducto;
    private String nombreProducto;
    private String descripcion;
    private BigDecimal precio;
    private int cantidad;
    private Proveedor proveedor; // Relación de objeto en lugar de solo el ID

    public Producto() {
    }

    public Producto(int idProducto, String nombreProducto, String descripcion, BigDecimal precio, int cantidad, Proveedor proveedor) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.descripcion = descripcion;
        this.precio = precio;
        this.cantidad = cantidad;
        this.proveedor = proveedor;
    }

    // Getters y Setters
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }
}
