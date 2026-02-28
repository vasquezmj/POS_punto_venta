package com.sellcontrol.model;

/**
 * Modelo que representa una merma (pérdida).
 */
public class Merma {

    private int id;
    private String descripcion;
    private double montoAproximado;
    private String fechaHora;

    public Merma() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getMontoAproximado() {
        return montoAproximado;
    }

    public void setMontoAproximado(double montoAproximado) {
        this.montoAproximado = montoAproximado;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }
}
