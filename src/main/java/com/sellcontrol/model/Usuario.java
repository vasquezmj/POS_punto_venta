package com.sellcontrol.model;

/**
 * Modelo que representa un usuario del sistema.
 */
public class Usuario {

    private int id;
    private String nombre;
    private String usuario;
    private String contrasena;
    private String rol; // ADMIN | CAJERO
    private boolean activo;
    private String creadoEn;

    public Usuario() {}

    public Usuario(int id, String nombre, String usuario, String contrasena, String rol, boolean activo, String creadoEn) {
        this.id = id;
        this.nombre = nombre;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.activo = activo;
        this.creadoEn = creadoEn;
    }

    // --- Getters y Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getCreadoEn() { return creadoEn; }
    public void setCreadoEn(String creadoEn) { this.creadoEn = creadoEn; }

    @Override
    public String toString() {
        return nombre + " (" + usuario + ") - " + rol;
    }
}
