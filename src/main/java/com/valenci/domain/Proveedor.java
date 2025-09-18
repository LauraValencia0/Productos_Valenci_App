package com.valenci.domain;

public class Proveedor extends Usuario {

    private String nombreEmpresa;

    public Proveedor() {
        super();
    }

    public Proveedor(int id, String nombre, String correo, String contrasena, String nombreEmpresa) {
        super(id, nombre, correo, contrasena);
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }
}