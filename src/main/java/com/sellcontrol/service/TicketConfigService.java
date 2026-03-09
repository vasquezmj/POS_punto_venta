package com.sellcontrol.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;

/**
 * Servicio para gestionar la configuración del ticket de venta.
 * Los valores se guardan en un archivo 'ticket.properties' junto a la base de
 * datos.
 */
public class TicketConfigService {

    private static final String CONFIG_FILE = "ticket.properties";

    // Claves
    private static final String KEY_NOMBRE_NEGOCIO = "nombre_negocio";
    private static final String KEY_SUBTITULO = "subtitulo";
    private static final String KEY_DIRECCION = "direccion";
    private static final String KEY_TELEFONO = "telefono";
    private static final String KEY_CEDULA = "cedula";
    private static final String KEY_DATO_OPCIONAL_1 = "dato_opcional_1";
    private static final String KEY_DATO_OPCIONAL_2 = "dato_opcional_2";
    private static final String KEY_PIE = "pie";

    // Valores por defecto
    private static final String DEF_NOMBRE = "VERDULERIA VL";
    private static final String DEF_SUBTITULO = "SellControl POS";
    private static final String DEF_DIRECCION = "";
    private static final String DEF_TELEFONO = "";
    private static final String DEF_CEDULA = "";
    private static final String DEF_DATO_1 = "";
    private static final String DEF_DATO_2 = "";
    private static final String DEF_PIE = "Gracias por su compra!";

    private final Properties props = new Properties();

    public TicketConfigService() {
        cargar();
    }

    /** Carga las propiedades del archivo. Si no existe, usa valores por defecto. */
    private void cargar() {
        Path path = Paths.get(CONFIG_FILE);
        if (Files.exists(path)) {
            try (Reader r = new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8)) {
                props.load(r);
            } catch (IOException e) {
                System.err.println("[TicketConfig] Error al cargar config: " + e.getMessage());
            }
        }
        // Asegurar valores por defecto
        props.putIfAbsent(KEY_NOMBRE_NEGOCIO, DEF_NOMBRE);
        props.putIfAbsent(KEY_SUBTITULO, DEF_SUBTITULO);
        props.putIfAbsent(KEY_DIRECCION, DEF_DIRECCION);
        props.putIfAbsent(KEY_TELEFONO, DEF_TELEFONO);
        props.putIfAbsent(KEY_CEDULA, DEF_CEDULA);
        props.putIfAbsent(KEY_DATO_OPCIONAL_1, DEF_DATO_1);
        props.putIfAbsent(KEY_DATO_OPCIONAL_2, DEF_DATO_2);
        props.putIfAbsent(KEY_PIE, DEF_PIE);
    }

    /** Guarda las propiedades actuales al archivo. */
    public void guardar() {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            props.store(w, "Configuración del ticket de venta");
            System.out.println("[TicketConfig] Configuración guardada.");
        } catch (IOException e) {
            System.err.println("[TicketConfig] Error al guardar config: " + e.getMessage());
        }
    }

    // --- Getters ---
    public String getNombreNegocio() {
        return props.getProperty(KEY_NOMBRE_NEGOCIO, DEF_NOMBRE);
    }

    public String getSubtitulo() {
        return props.getProperty(KEY_SUBTITULO, DEF_SUBTITULO);
    }

    public String getDireccion() {
        return props.getProperty(KEY_DIRECCION, DEF_DIRECCION);
    }

    public String getTelefono() {
        return props.getProperty(KEY_TELEFONO, DEF_TELEFONO);
    }

    public String getPie() {
        return props.getProperty(KEY_PIE, DEF_PIE);
    }

    public String getCedula() {
        return props.getProperty(KEY_CEDULA, DEF_CEDULA);
    }

    public String getDatoOpcional1() {
        return props.getProperty(KEY_DATO_OPCIONAL_1, DEF_DATO_1);
    }

    public String getDatoOpcional2() {
        return props.getProperty(KEY_DATO_OPCIONAL_2, DEF_DATO_2);
    }

    // --- Setters ---
    public void setNombreNegocio(String val) {
        props.setProperty(KEY_NOMBRE_NEGOCIO, val != null ? val : "");
    }

    public void setSubtitulo(String val) {
        props.setProperty(KEY_SUBTITULO, val != null ? val : "");
    }

    public void setDireccion(String val) {
        props.setProperty(KEY_DIRECCION, val != null ? val : "");
    }

    public void setTelefono(String val) {
        props.setProperty(KEY_TELEFONO, val != null ? val : "");
    }

    public void setCedula(String val) {
        props.setProperty(KEY_CEDULA, val != null ? val : "");
    }

    public void setDatoOpcional1(String val) {
        props.setProperty(KEY_DATO_OPCIONAL_1, val != null ? val : "");
    }

    public void setDatoOpcional2(String val) {
        props.setProperty(KEY_DATO_OPCIONAL_2, val != null ? val : "");
    }

    public void setPie(String val) {
        props.setProperty(KEY_PIE, val != null ? val : "");
    }
}
