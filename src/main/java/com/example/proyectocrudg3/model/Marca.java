package com.example.proyectocrudg3.model;

public class Marca {
    private int id_marca;
    private String nombre;
    private String descripcion;
    private int estado;

    public Marca() {
    }

    public Marca(int id_marca, String nombre) {
        this.id_marca = id_marca;
        this.nombre = nombre;
    }

    public Marca(int id_marca, String nombre, String descripcion, int estado) {
        this.id_marca = id_marca;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public int getId_marca() {
        return id_marca;
    }

    public void setId_marca(int id_marca) {
        this.id_marca = id_marca;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return nombre; // Permite que el ComboBox muestre el nombre del objeto directamente
    }
}