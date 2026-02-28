package com.sellcontrol.controller;

import com.sellcontrol.App;
import com.sellcontrol.model.MovimientoCaja;
import com.sellcontrol.service.CajaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controlador del módulo de Caja.
 */
public class CajaController {

    @FXML
    private ComboBox<String> cmbTipoMov;
    @FXML
    private TextField txtMonto;
    @FXML
    private TextField txtMotivo;
    @FXML
    private TableView<MovimientoCaja> tablaMovimientos;
    @FXML
    private TableColumn<MovimientoCaja, Integer> colMId;
    @FXML
    private TableColumn<MovimientoCaja, String> colMTipo;
    @FXML
    private TableColumn<MovimientoCaja, String> colMMonto;
    @FXML
    private TableColumn<MovimientoCaja, String> colMMotivo;
    @FXML
    private TableColumn<MovimientoCaja, String> colMUsuario;
    @FXML
    private TableColumn<MovimientoCaja, String> colMFecha;
    @FXML
    private Label lblMensaje;

    private final CajaService cajaService = new CajaService();

    @FXML
    public void initialize() {
        cmbTipoMov.setItems(FXCollections.observableArrayList("INGRESO", "CAMBIO"));
        cmbTipoMov.setValue("INGRESO");

        colMId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colMMonto.setCellValueFactory(c -> new SimpleStringProperty(String.format("₡%.2f", c.getValue().getMonto())));
        colMMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colMUsuario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreUsuario()));
        colMFecha.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));

        cargarDatos();
    }

    private void cargarDatos() {
        tablaMovimientos.setItems(FXCollections.observableArrayList(cajaService.movimientosDeHoy()));
    }

    @FXML
    private void handleRegistrar() {
        String error = cajaService.registrarMovimiento(cmbTipoMov.getValue(), txtMonto.getText(), txtMotivo.getText());
        if (error == null) {
            mostrarMensaje("Movimiento registrado.", false);
            txtMonto.clear();
            txtMotivo.clear();
            cargarDatos();
        } else {
            mostrarMensaje(error, true);
        }
    }

    @FXML
    private void handleVolver() {
        App.changeScene("dashboard.fxml", "Panel Principal", 900, 600);
    }

    private void mostrarMensaje(String msg, boolean esError) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(esError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}
