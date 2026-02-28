package com.sellcontrol.model;

/**
 * Modelo que representa un producto de la verdulería.
 * Un producto se vende por kg O por unidad (no ambos).
 */
public class Producto {

    private int id;
    private String nombre;
    private String tipo; // FRUTA | VERDURA | OTRO
    private Double precioPorKg;
    private Double precioPorUnidad;
    private boolean activo;
    private String creadoEn;

    public Producto() {
    }

    public Producto(int id, String nombre, String tipo, Double precioPorKg, Double precioPorUnidad, boolean activo,
            String creadoEn) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.precioPorKg = precioPorKg;
        this.precioPorUnidad = precioPorUnidad;
        this.activo = activo;
        this.creadoEn = creadoEn;
    }

    // --- Getters y Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getPrecioPorKg() {
        return precioPorKg;
    }

    public void setPrecioPorKg(Double precioPorKg) {
        this.precioPorKg = precioPorKg;
    }

    public Double getPrecioPorUnidad() {
        return precioPorUnidad;
    }

    public void setPrecioPorUnidad(Double precioPorUnidad) {
        this.precioPorUnidad = precioPorUnidad;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(String creadoEn) {
        this.creadoEn = creadoEn;
    }

    /**
     * @return true si el producto se vende por kg.
     */
    public boolean isVentaPorKg() {
        return precioPorKg != null && precioPorKg > 0;
    }

    /**
     * @return el precio activo del producto (por kg o por unidad).
     */
    public double getPrecioActivo() {
        if (isVentaPorKg())
            return precioPorKg;
        return precioPorUnidad != null ? precioPorUnidad : 0;
    }

    /**
     * @return la unidad de venta como texto ("Kg" o "Unidad").
     */
    public String getUnidadVenta() {
        return isVentaPorKg() ? "Kg" : "Unidad";
    }

    @Override
    public String toString() {
        return nombre + " (" + tipo + ") - " + getUnidadVenta() + ": $" + String.format("%.2f", getPrecioActivo());
    }
}
