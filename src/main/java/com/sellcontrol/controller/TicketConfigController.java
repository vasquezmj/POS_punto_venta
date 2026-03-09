package com.sellcontrol.controller;

import com.sellcontrol.App;
import com.sellcontrol.service.TicketConfigService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Controlador para la pantalla de configuración del ticket.
 * Permite editar los textos del ticket y ver una vista previa en tiempo real.
 */
public class TicketConfigController {

    @FXML
    private TextField txtNombreNegocio;
    @FXML
    private TextField txtSubtitulo;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtCedula;
    @FXML
    private TextField txtDatoOpcional1;
    @FXML
    private TextField txtDatoOpcional2;
    @FXML
    private TextField txtPie;
    @FXML
    private VBox vboxPreview;
    @FXML
    private Label lblMensaje;

    private final TicketConfigService configService = new TicketConfigService();

    @FXML
    public void initialize() {
        // Cargar valores actuales
        txtNombreNegocio.setText(configService.getNombreNegocio());
        txtSubtitulo.setText(configService.getSubtitulo());
        txtDireccion.setText(configService.getDireccion());
        txtTelefono.setText(configService.getTelefono());
        txtCedula.setText(configService.getCedula());
        txtDatoOpcional1.setText(configService.getDatoOpcional1());
        txtDatoOpcional2.setText(configService.getDatoOpcional2());
        txtPie.setText(configService.getPie());

        // Listeners para actualizar vista previa en tiempo real
        txtNombreNegocio.textProperty().addListener((o, ov, nv) -> actualizarPreview());
        txtSubtitulo.textProperty().addListener((o, ov, nv) -> actualizarPreview());
        txtDireccion.textProperty().addListener((o, ov, nv) -> actualizarPreview());
        txtTelefono.textProperty().addListener((o, ov, nv) -> actualizarPreview());
        txtCedula.textProperty().addListener((o, ov, nv) -> actualizarPreview());
        txtDatoOpcional1.textProperty().addListener((o, ov, nv) -> actualizarPreview());
        txtDatoOpcional2.textProperty().addListener((o, ov, nv) -> actualizarPreview());
        txtPie.textProperty().addListener((o, ov, nv) -> actualizarPreview());

        actualizarPreview();
    }

    /** Actualiza la vista previa del ticket en tiempo real. */
    private void actualizarPreview() {
        vboxPreview.getChildren().clear();

        String separador = "================================";
        String separadorFino = "--------------------------------";

        // Encabezado
        agregarLabel(txtNombreNegocio.getText(), true, 16, TextAlignment.CENTER);
        agregarLabel(txtSubtitulo.getText(), false, 11, TextAlignment.CENTER);

        String dir = txtDireccion.getText();
        if (dir != null && !dir.isBlank()) {
            agregarLabel(dir, false, 10, TextAlignment.CENTER);
        }

        String tel = txtTelefono.getText();
        if (tel != null && !tel.isBlank()) {
            agregarLabel(tel, false, 10, TextAlignment.CENTER);
        }

        String ced = txtCedula.getText();
        if (ced != null && !ced.isBlank()) {
            agregarLabel(ced, false, 10, TextAlignment.CENTER);
        }

        agregarLabel(separador, false, 10, TextAlignment.CENTER);

        // Datos opcionales
        String dato1 = txtDatoOpcional1.getText();
        if (dato1 != null && !dato1.isBlank()) {
            agregarLabel(dato1, false, 10, TextAlignment.LEFT);
        }
        String dato2 = txtDatoOpcional2.getText();
        if (dato2 != null && !dato2.isBlank()) {
            agregarLabel(dato2, false, 10, TextAlignment.LEFT);
        }

        // Datos simulados
        agregarLabel("Fecha  : 2026-03-09 12:00", false, 10, TextAlignment.LEFT);
        agregarLabel("Cajero : Admin", false, 10, TextAlignment.LEFT);
        agregarLabel("Pago   : EFECTIVO", false, 10, TextAlignment.LEFT);
        agregarLabel(separadorFino, false, 10, TextAlignment.LEFT);
        agregarLabel("Producto         Cant    Subt.", true, 10, TextAlignment.LEFT);
        agregarLabel(separadorFino, false, 10, TextAlignment.LEFT);
        agregarLabel("Tomate           2.0kg     1000", false, 10, TextAlignment.LEFT);
        agregarLabel("Cebolla          1.0kg      500", false, 10, TextAlignment.LEFT);
        agregarLabel(separador, false, 10, TextAlignment.CENTER);
        agregarLabel("TOTAL: ₡1500", true, 14, TextAlignment.CENTER);
        agregarLabel(separador, false, 10, TextAlignment.CENTER);

        // Pie
        agregarLabel("", false, 10, TextAlignment.CENTER);
        agregarLabel(txtPie.getText(), false, 11, TextAlignment.CENTER);
        agregarLabel("", false, 10, TextAlignment.CENTER);
    }

    /** Agrega un label estilizado a la vista previa. */
    private void agregarLabel(String text, boolean bold, double fontSize, TextAlignment align) {
        Label lbl = new Label(text != null && !text.isBlank() ? text : " ");
        lbl.setFont(Font.font("Courier New", bold ? FontWeight.BOLD : FontWeight.NORMAL, fontSize));
        lbl.setTextAlignment(align);
        lbl.setMaxWidth(Double.MAX_VALUE);

        String alignment = switch (align) {
            case CENTER -> "-fx-alignment: center;";
            case RIGHT -> "-fx-alignment: center-right;";
            default -> "-fx-alignment: center-left;";
        };
        lbl.setStyle(alignment + " -fx-text-fill: #333;");

        vboxPreview.getChildren().add(lbl);
    }

    @FXML
    private void handleGuardar() {
        configService.setNombreNegocio(txtNombreNegocio.getText());
        configService.setSubtitulo(txtSubtitulo.getText());
        configService.setDireccion(txtDireccion.getText());
        configService.setTelefono(txtTelefono.getText());
        configService.setCedula(txtCedula.getText());
        configService.setDatoOpcional1(txtDatoOpcional1.getText());
        configService.setDatoOpcional2(txtDatoOpcional2.getText());
        configService.setPie(txtPie.getText());
        configService.guardar();

        lblMensaje.setText("✅ Configuración guardada exitosamente.");
        lblMensaje.setStyle("-fx-text-fill: #27ae60;");
    }

    @FXML
    private void handleRestaurar() {
        txtNombreNegocio.setText("VERDULERIA VL");
        txtSubtitulo.setText("SellControl POS");
        txtDireccion.setText("");
        txtTelefono.setText("");
        txtCedula.setText("");
        txtDatoOpcional1.setText("");
        txtDatoOpcional2.setText("");
        txtPie.setText("Gracias por su compra!");

        lblMensaje.setText("ℹ Valores restaurados. Presione Guardar para aplicar.");
        lblMensaje.setStyle("-fx-text-fill: #2980b9;");
    }

    @FXML
    private void handleVolver() {
        App.changeScene("dashboard.fxml", "Panel Principal", 900, 600);
    }
}
