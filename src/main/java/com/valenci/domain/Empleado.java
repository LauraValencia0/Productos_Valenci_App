package com.valenci.domain;

import java.math.BigDecimal;

public class Empleado extends Usuario {

    private String cargo;
    private BigDecimal salario;

    public Empleado() {
        super();
    }

    public Empleado(int id, String nombre, String correo, String contrasena, String cargo, BigDecimal salario) {
        super(id, nombre, correo, contrasena);
        this.cargo = cargo;
        this.salario = salario;
    }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public BigDecimal getSalario() { return salario; }
    public void setSalario(BigDecimal salario) { this.salario = salario; }
}