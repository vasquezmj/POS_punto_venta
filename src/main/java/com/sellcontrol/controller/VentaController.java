package com.sellcontrol.controller;

import com.sellcontrol.App;
import com.sellcontrol.model.DetalleVenta;
import com.sellcontrol.model.Producto;
import com.sellcontrol.model.Venta;
import com.sellcontrol.service.ProductoService;
import com.sellcontrol.service.TicketPrintService;
import com.sellcontrol.service.VentaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para el módulo de ventas.
 * Permite crear ventas rápidas, ver ventas del día y cobrar fiados.
 */
public class VentaController {

    // --- Pestaña Ventas Activas ---
    @FXML
    private TabPane tabPaneActivas;

    // --- Pestaña Ventas del Día ---
    @FXML
    private TableView<Venta> tablaVentasHoy;
    @FXML
    private TableColumn<Venta, Integer> colVId;
    @FXML
    private TableColumn<Venta, String> colVFecha;
    @FXML
    private TableColumn<Venta, String> colVUsuario;
    @FXML
    private TableColumn<Venta, String> colVTotal;
    @FXML
    private TableColumn<Venta, String> colVMetodo;
    @FXML
    private TableColumn<Venta, String> colVEstado;
    @FXML
    private TableColumn<Venta, String> colVCliente;

    // --- Pestaña Fiados ---
    @FXML
    private TableView<Venta> tablaFiados;
    @FXML
    private TableColumn<Venta, Integer> colFId;
    @FXML
    private TableColumn<Venta, String> colFFecha;
    @FXML
    private TableColumn<Venta, String> colFCliente;
    @FXML
    private TableColumn<Venta, String> colFTotal;
    @FXML
    private TableColumn<Venta, String> colFUsuario;

    @FXML
    private Label lblMensaje;

    private final VentaService ventaService = new VentaService();
    private final TicketPrintService ticketPrintService = new TicketPrintService();

    @FXML
    public void initialize() {
        // Configurar tabla ventas del día
        colVId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colVFecha.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
        colVUsuario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreUsuario()));
        colVTotal.setCellValueFactory(c -> new SimpleStringProperty(String.format("₡%.2f", c.getValue().getTotal())));
        colVMetodo.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
        colVEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colVCliente.setCellValueFactory(c -> {
            String cn = c.getValue().getClienteNombre();
            return new SimpleStringProperty(cn != null ? cn : "—");
        });

        // Configurar tabla fiados
        colFId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFFecha.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
        colFCliente.setCellValueFactory(c -> {
            String cn = c.getValue().getClienteNombre();
            return new SimpleStringProperty(cn != null ? cn : "Sin nombre");
        });
        colFTotal.setCellValueFactory(c -> new SimpleStringProperty(String.format("₡%.2f", c.getValue().getTotal())));
        colFUsuario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreUsuario()));

        cargarVentasHoy();
        cargarFiados();

        // Abrir la primera pestaña de venta automáticamente al iniciar
        handleNuevaVentaTab();
    }

    @FXML
    public void handleNuevaVentaTab() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/sellcontrol/fxml/venta_tab.fxml"));
            javafx.scene.Parent tabContent = loader.load();
            VentaTabController tabController = loader.getController();
            tabController.setParentController(this);

            int numero = calcularSiguienteNumeroTab();
            Tab tab = new Tab("Venta " + numero);
            tab.setContent(tabContent);
            tab.setUserData(tabController);
            tabController.setTab(tab);

            // Al cerrar la pestaña, verificar si el carrito tiene productos
            tab.setOnCloseRequest(event -> {
                if (!tabController.isCartEmpty()) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmar Cierre");
                    confirm.setHeaderText("La pestaña tiene productos en el carrito.");
                    confirm.setContentText("¿Está seguro de que desea cerrar esta pestaña? Se perderán los cambios.");
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        event.consume(); // Cancelar cierre
                    }
                }
            });

            tabPaneActivas.getTabs().add(tab);
            tabPaneActivas.getSelectionModel().select(tab);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensaje("Error al crear pestaña de venta: " + e.getMessage(), true);
        }
    }

    /**
     * Calcula el número más bajo disponible para nombrar una nueva pestaña de venta.
     * Si existen "Venta 1" y "Venta 3", retorna 2.
     */
    private int calcularSiguienteNumeroTab() {
        java.util.Set<Integer> usados = new java.util.HashSet<>();
        for (Tab tab : tabPaneActivas.getTabs()) {
            String titulo = tab.getText();
            if (titulo != null && titulo.startsWith("Venta ")) {
                try {
                    usados.add(Integer.parseInt(titulo.substring(6)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        int numero = 1;
        while (usados.contains(numero)) {
            numero++;
        }
        return numero;
    }

    public void recargarVentasYFiados() {
        cargarVentasHoy();
        cargarFiados();
    }

    /**
     * Cierra la pestaña de venta después de registrar una venta exitosa.
     * Siempre garantiza que quede al menos una pestaña de venta abierta.
     */
    public void cerrarTabDespuesDeVenta(Tab tab) {
        if (tab == null) return;

        if (tabPaneActivas.getTabs().size() <= 1) {
            // Es la única pestaña: no cerrar, ya quedó limpia
            return;
        }

        // Hay más de una pestaña, cerrar esta
        tabPaneActivas.getTabs().remove(tab);
    }

    @FXML
    private void handleCobrarFiado() {
        Venta selected = tablaFiados.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarMensaje("Seleccione una venta pendiente.", true);
            return;
        }

        double totalFiado = selected.getTotal();
        String cliente = selected.getClienteNombre() != null ? selected.getClienteNombre() : "cliente";

        // Preguntar método de pago
        ChoiceDialog<String> dlgMetodo = new ChoiceDialog<>("EFECTIVO", "EFECTIVO", "TARJETA", "SINPE");
        dlgMetodo.setTitle("Cobrar Venta Fiada");
        dlgMetodo.setHeaderText("Venta #" + selected.getId() + " de " + cliente
                + "\nTotal: ₡" + String.format("%.2f", totalFiado));
        dlgMetodo.setContentText("Método de pago:");

        Optional<String> metodoResult = dlgMetodo.showAndWait();
        if (metodoResult.isEmpty()) {
            return; // Canceló
        }

        String metodoPago = metodoResult.get();

        // Si es efectivo, pedir monto y calcular cambio
        if ("EFECTIVO".equals(metodoPago)) {
            TextInputDialog dlgPago = new TextInputDialog();
            dlgPago.setTitle("Pago en Efectivo");
            dlgPago.setHeaderText(String.format("Total a cobrar: ₡%.2f", totalFiado));
            dlgPago.setContentText("¿Con cuánto paga el cliente? ₡");

            Optional<String> resultado = dlgPago.showAndWait();
            if (resultado.isEmpty() || resultado.get().isBlank()) {
                mostrarMensaje("Cobro cancelado.", true);
                return;
            }

            double montoPagado;
            try {
                montoPagado = Double.parseDouble(resultado.get().replace(",", "."));
            } catch (NumberFormatException e) {
                mostrarMensaje("El monto ingresado no es válido.", true);
                return;
            }

            if (montoPagado < totalFiado) {
                mostrarMensaje(String.format("El monto (₡%.2f) es menor al total (₡%.2f).", montoPagado, totalFiado),
                        true);
                return;
            }

            double cambio = montoPagado - totalFiado;

            // Cobrar la venta
            String error = ventaService.cobrarVenta(selected.getId(), metodoPago);
            if (error != null) {
                mostrarMensaje(error, true);
                return;
            }

            // Abrir cajón de dinero
            ticketPrintService.abrirCajon();

            // Mostrar cambio
            Dialog<Void> dlgCambio = new Dialog<>();
            dlgCambio.setTitle("💰 Cambio");
            dlgCambio.setHeaderText(null);

            VBox contenido = new VBox(12);
            contenido.setAlignment(Pos.CENTER);
            contenido.setPadding(new Insets(20, 30, 20, 30));
            contenido.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

            Label lblTituloCambio = new Label("✅ Venta fiada cobrada");
            lblTituloCambio.setFont(Font.font("System", FontWeight.BOLD, 16));
            lblTituloCambio.setStyle("-fx-text-fill: #2c3e50;");

            Label lblTotalDlg = new Label(String.format("Total: ₡%.2f", totalFiado));
            lblTotalDlg.setFont(Font.font("System", FontWeight.NORMAL, 18));
            lblTotalDlg.setStyle("-fx-text-fill: #555;");

            Label lblPagoCon = new Label(String.format("Pagó con: ₡%.2f", montoPagado));
            lblPagoCon.setFont(Font.font("System", FontWeight.NORMAL, 18));
            lblPagoCon.setStyle("-fx-text-fill: #555;");

            Separator sep = new Separator();

            Label lblCambioTitulo = new Label("CAMBIO A DEVOLVER");
            lblCambioTitulo.setFont(Font.font("System", FontWeight.BOLD, 14));
            lblCambioTitulo.setStyle("-fx-text-fill: #7f8c8d;");

            Label lblCambioValor = new Label(String.format("₡%.2f", cambio));
            lblCambioValor.setFont(Font.font("System", FontWeight.BOLD, 36));
            lblCambioValor.setStyle("-fx-text-fill: #27ae60;");

            contenido.getChildren().addAll(lblTituloCambio, lblTotalDlg, lblPagoCon, sep, lblCambioTitulo,
                    lblCambioValor);

            dlgCambio.getDialogPane().setContent(contenido);
            dlgCambio.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dlgCambio.getDialogPane().setMinWidth(420);
            dlgCambio.getDialogPane().setMinHeight(300);
            dlgCambio.showAndWait();

        } else {
            // Tarjeta / SINPE: solo confirmar
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Cobrar Venta Fiada");
            confirm.setHeaderText("¿Cobrar venta #" + selected.getId() + " de " + cliente
                    + " por ₡" + String.format("%.2f", totalFiado) + " con " + metodoPago + "?");
            Optional<ButtonType> ans = confirm.showAndWait();
            if (ans.isEmpty() || ans.get() != ButtonType.OK) {
                return;
            }

            String error = ventaService.cobrarVenta(selected.getId(), metodoPago);
            if (error != null) {
                mostrarMensaje(error, true);
                return;
            }
        }

        mostrarMensaje("Venta #" + selected.getId() + " cobrada con " + metodoPago + ".", false);
        cargarVentasHoy();
        cargarFiados();
    }

    @FXML
    private void handleVerDetalle() {
        Venta selected = tablaVentasHoy.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarMensaje("Seleccione una venta.", true);
            return;
        }

        List<DetalleVenta> detalles = ventaService.obtenerDetalles(selected.getId());
        StringBuilder sb = new StringBuilder();
        sb.append("Venta #").append(selected.getId()).append("\n");
        sb.append("Fecha: ").append(selected.getFechaHora()).append("\n");
        sb.append("Cajero: ").append(selected.getNombreUsuario()).append("\n");
        sb.append("Método: ").append(selected.getMetodoPago()).append("\n");
        sb.append("Estado: ").append(selected.getEstado()).append("\n\n");
        sb.append("─── Productos ───\n");
        for (DetalleVenta dv : detalles) {
            sb.append(String.format("  %s  x%.2f %s  = ₡%.2f\n",
                    dv.getNombreProducto(), dv.getCantidad(), dv.getTipoUnidad(), dv.getSubtotal()));
        }
        sb.append("\nTotal: ₡").append(String.format("%.2f", selected.getTotal()));

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Detalle de Venta");
        info.setHeaderText("Venta #" + selected.getId());
        TextArea ta = new TextArea(sb.toString());
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setPrefRowCount(12);
        info.getDialogPane().setContent(ta);
        info.showAndWait();
    }

    @FXML
    private void handleImprimirTicket() {
        Venta selected = tablaVentasHoy.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarMensaje("Seleccione una venta para imprimir.", true);
            return;
        }

        List<DetalleVenta> detalles = ventaService.obtenerDetalles(selected.getId());
        String cajero = selected.getNombreUsuario() != null ? selected.getNombreUsuario() : "—";

        String error = ticketPrintService.imprimir(selected, detalles, cajero);
        if (error == null) {
            mostrarMensaje("✅ Ticket de venta #" + selected.getId() + " enviado a imprimir.", false);
        } else {
            mostrarMensaje(error, true);
        }
    }

    private void cargarVentasHoy() {
        List<Venta> ventas = ventaService.ventasDeHoy();
        tablaVentasHoy.setItems(FXCollections.observableArrayList(ventas));
    }

    private void cargarFiados() {
        List<Venta> fiados = ventaService.ventasPendientes();
        tablaFiados.setItems(FXCollections.observableArrayList(fiados));
    }

    @FXML
    private void handleRefrescar() {
        recargarVentasYFiados();
        // Recargar productos en todas las pestañas de ventas activas
        for (Tab tab : tabPaneActivas.getTabs()) {
            Object data = tab.getUserData();
            if (data instanceof VentaTabController) {
                ((VentaTabController) data).recargarProductos();
            }
        }
        mostrarMensaje("Datos actualizados.", false);
    }

    @FXML
    private void handleVolver() {
        App.changeScene("dashboard.fxml", "Panel Principal", 900, 600);
    }

    public void mostrarMensaje(String msg, boolean esError) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(esError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}
