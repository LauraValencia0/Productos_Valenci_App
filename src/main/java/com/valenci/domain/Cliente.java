package com.valenci.domain;

public class Cliente extends Usuario {

    private String direccionEnvio;

    public Cliente() {
        super();
    }

    public Cliente(int id, String nombre, String correo, String contrasena, String direccionEnvio) {
        super(id, nombre, correo, contrasena);
        this.direccionEnvio = direccionEnvio;
    }

    public String getDireccionEnvio() { return direccionEnvio; }
    public void setDireccionEnvio(String direccionEnvio) { this.direccionEnvio = direccionEnvio; }
}