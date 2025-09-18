package com.valenci.domain;

// Esta clase no tiene atributos propios, su rol lo define la base de datos.
// Sirve para diferenciar el tipo de objeto en el código.
public class Administrador extends Usuario {

    public Administrador() {
        super();
    }

    public Administrador(int id, String nombre, String correo, String contrasena) {
        super(id, nombre, correo, contrasena);
    }
}