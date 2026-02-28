package com.sellcontrol.controller;

import com.sellcontrol.App;
import com.sellcontrol.model.Gasto;
import com.sellcontrol.model.Merma;
import com.sellcontrol.service.GastoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controlador del módulo de Gastos y Merma.
 */
public class GastoController {

    // Gastos
    @FXML
    private ComboBox<String> cmbTipoGasto;
    @FXML
    private TextField txtMontoGasto;
    @FXML
    private TextField txtDescGasto;
    @FXML
    private TableView<Gasto> tablaGastos;
    @FXML
    private TableColumn<Gasto, Integer> colGId;
    @FXML
    private TableColumn<Gasto, String> colGTipo;
    @FXML
    private TableColumn<Gasto, String> colGMonto;
    @FXML
    private TableColumn<Gasto, String> colGDesc;
    @FXML
    private TableColumn<Gasto, String> colGFecha;

    // Merma
    @FXML
    private TextField txtDescMerma;
    @FXML
    private TextField txtMontoMerma;
    @FXML
    private TableView<Merma> tablaMermas;
    @FXML
    private TableColumn<Merma, Integer> colMeId;
    @FXML
    private TableColumn<Merma, String> colMeDesc;
    @FXML
    private TableColumn<Merma, String> colMeMonto;
    @FXML
    private TableColumn<Merma, String> colMeFecha;

    @FXML
    private Label lblMensaje;

    private final GastoService gastoService = new GastoService();

    @FXML
    public void initialize() {
        cmbTipoGasto.setItems(FXCollections.observableArrayList("ABASTECIMIENTO", "EMPLEADOS"));
        cmbTipoGasto.setValue("ABASTECIMIENTO");

        // Columnas gastos
        colGId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colGTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colGMonto.setCellValueFactory(c -> new SimpleStringProperty(String.format("₡%.2f", c.getValue().getMonto())));
        colGDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colGFecha.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));

        // Columnas merma
        colMeId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMeDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colMeMonto.setCellValueFactory(
                c -> new SimpleStringProperty(String.format("₡%.2f", c.getValue().getMontoAproximado())));
        colMeFecha.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));

        cargarDatos();
    }

    private void cargarDatos() {
        tablaGastos.setItems(FXCollections.observableArrayList(gastoService.gastosDeHoy()));
        tablaMermas.setItems(FXCollections.observableArrayList(gastoService.mermasDeHoy()));
    }

    @FXML
    private void handleRegistrarGasto() {
        String error = gastoService.registrarGasto(cmbTipoGasto.getValue(), txtMontoGasto.getText(),
                txtDescGasto.getText());
        if (error == null) {
            mostrarMensaje("Gasto registrado.", false);
            txtMontoGasto.clear();
            txtDescGasto.clear();
            cargarDatos();
        } else {
            mostrarMensaje(error, true);
        }
    }

    @FXML
    private void handleRegistrarMerma() {
        String error = gastoService.registrarMerma(txtDescMerma.getText(), txtMontoMerma.getText());
        if (error == null) {
            mostrarMensaje("Merma registrada.", false);
            txtDescMerma.clear();
            txtMontoMerma.clear();
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
