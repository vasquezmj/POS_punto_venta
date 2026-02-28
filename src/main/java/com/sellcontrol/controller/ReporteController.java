package com.sellcontrol.controller;

import com.sellcontrol.App;
import com.sellcontrol.model.Venta;
import com.sellcontrol.service.ReporteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador del módulo de Reportes.
 */
public class ReporteController {

    @FXML
    private DatePicker dpDesde;
    @FXML
    private DatePicker dpHasta;

    // Resumen
    @FXML
    private Label lblTotalVentas;
    @FXML
    private Label lblVentasCobradas;
    @FXML
    private Label lblVentasPendientes;
    @FXML
    private Label lblEfectivo;
    @FXML
    private Label lblTarjeta;
    @FXML
    private Label lblSinpe;
    @FXML
    private Label lblGastos;
    @FXML
    private Label lblMerma;
    @FXML
    private Label lblGanancia;

    // Tabla de ventas
    @FXML
    private TableView<Venta> tablaVentas;
    @FXML
    private TableColumn<Venta, Integer> colId;
    @FXML
    private TableColumn<Venta, String> colFecha;
    @FXML
    private TableColumn<Venta, String> colCajero;
    @FXML
    private TableColumn<Venta, String> colTotal;
    @FXML
    private TableColumn<Venta, String> colMetodo;
    @FXML
    private TableColumn<Venta, String> colEstado;

    @FXML
    private Label lblMensaje;

    private final ReporteService reporteService = new ReporteService();

    @FXML
    public void initialize() {
        // Fechas por defecto: hoy
        dpDesde.setValue(LocalDate.now());
        dpHasta.setValue(LocalDate.now());

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
        colCajero.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreUsuario()));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(String.format("₡%.2f", c.getValue().getTotal())));
        colMetodo.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        generarReporte();
    }

    @FXML
    private void handleGenerarReporte() {
        generarReporte();
    }

    @FXML
    private void handleHoy() {
        dpDesde.setValue(LocalDate.now());
        dpHasta.setValue(LocalDate.now());
        generarReporte();
    }

    @FXML
    private void handleSemana() {
        dpHasta.setValue(LocalDate.now());
        dpDesde.setValue(LocalDate.now().minusDays(6));
        generarReporte();
    }

    private void generarReporte() {
        try {
            LocalDate desde = dpDesde.getValue();
            LocalDate hasta = dpHasta.getValue();
            if (desde == null || hasta == null) {
                lblMensaje.setText("Seleccione rango de fechas.");
                return;
            }

            String desdeStr = desde.toString();
            String hastaStr = hasta.toString();
            System.out.println("[Reportes] Generando reporte: " + desdeStr + " a " + hastaStr);

            // Ventas
            List<Venta> ventas = reporteService.ventasPorRango(desdeStr, hastaStr);
            tablaVentas.setItems(FXCollections.observableArrayList(ventas));

            double totalVentas = reporteService.totalVentasTodas(desdeStr, hastaStr);
            double cobradas = reporteService.totalVentasCobradas(desdeStr, hastaStr);
            double pendientes = totalVentas - cobradas;

            lblTotalVentas.setText(String.format("₡%.2f", totalVentas));
            lblVentasCobradas.setText(String.format("₡%.2f", cobradas));
            lblVentasPendientes.setText(String.format("₡%.2f", pendientes));

            // Por método de pago
            lblEfectivo
                    .setText(String.format("₡%.2f", reporteService.totalPorMetodoPago(desdeStr, hastaStr, "EFECTIVO")));
            lblTarjeta
                    .setText(String.format("₡%.2f", reporteService.totalPorMetodoPago(desdeStr, hastaStr, "TARJETA")));
            lblSinpe.setText(String.format("₡%.2f", reporteService.totalPorMetodoPago(desdeStr, hastaStr, "SINPE")));

            // Gastos y merma
            double gastos = reporteService.totalGastos(desdeStr, hastaStr);
            double merma = reporteService.totalMerma(desdeStr, hastaStr);
            double ganancia = reporteService.gananciaReal(desdeStr, hastaStr);

            lblGastos.setText(String.format("₡%.2f", gastos));
            lblMerma.setText(String.format("₡%.2f", merma));
            lblGanancia.setText(String.format("₡%.2f", ganancia));
            lblGanancia.setStyle(ganancia >= 0 ? "-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 18px;"
                    : "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 18px;");

            lblMensaje.setText("Reporte generado: " + ventas.size() + " ventas encontradas.");
            lblMensaje.setStyle("-fx-text-fill: #27ae60;");
            System.out.println("[Reportes] Ventas encontradas: " + ventas.size() + ", Total: " + totalVentas);
        } catch (Exception e) {
            System.err.println("[Reportes] Error: " + e.getMessage());
            e.printStackTrace();
            lblMensaje.setText("Error al generar reporte: " + e.getMessage());
            lblMensaje.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    private void handleVolver() {
        App.changeScene("dashboard.fxml", "Panel Principal", 900, 600);
    }
}
