package com.sellcontrol.controller;

import com.sellcontrol.App;
import com.sellcontrol.model.DetalleVenta;
import com.sellcontrol.model.Producto;
import com.sellcontrol.model.Venta;
import com.sellcontrol.service.ProductoService;
import com.sellcontrol.service.VentaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para el módulo de ventas.
 * Permite crear ventas rápidas, ver ventas del día y cobrar fiados.
 */
public class VentaController {

    // --- Pestaña Nueva Venta ---
    @FXML
    private ComboBox<Producto> cmbProducto;
    @FXML
    private TextField txtCantidad;
    @FXML
    private Label lblPrecioUnitario;
    @FXML
    private Label lblSubtotal;
    @FXML
    private TableView<DetalleVenta> tablaDetalle;
    @FXML
    private TableColumn<DetalleVenta, String> colDetProducto;
    @FXML
    private TableColumn<DetalleVenta, String> colDetCantidad;
    @FXML
    private TableColumn<DetalleVenta, String> colDetUnidad;
    @FXML
    private TableColumn<DetalleVenta, String> colDetSubtotal;
    @FXML
    private Label lblTotal;
    @FXML
    private ComboBox<String> cmbMetodoPago;
    @FXML
    private CheckBox chkFiado;
    @FXML
    private TextField txtClienteNombre;
    @FXML
    private Label lblClienteLabel;

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

    private final ProductoService productoService = new ProductoService();
    private final VentaService ventaService = new VentaService();
    private final ObservableList<DetalleVenta> detallesCarrito = FXCollections.observableArrayList();
    private double totalVenta = 0;

    @FXML
    public void initialize() {
        // Cargar productos activos en combo
        List<Producto> productosActivos = productoService.listarActivos();
        cmbProducto.setItems(FXCollections.observableArrayList(productosActivos));

        // Métodos de pago
        cmbMetodoPago.setItems(FXCollections.observableArrayList("EFECTIVO", "TARJETA", "SINPE"));
        cmbMetodoPago.setValue("EFECTIVO");

        // Configurar tabla del carrito
        colDetProducto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreProducto()));
        colDetCantidad
                .setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f", c.getValue().getCantidad())));
        colDetUnidad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoUnidad()));
        colDetSubtotal
                .setCellValueFactory(c -> new SimpleStringProperty(String.format("₡%.2f", c.getValue().getSubtotal())));
        tablaDetalle.setItems(detallesCarrito);

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

        // Listener para mostrar/ocultar campo cliente
        chkFiado.selectedProperty().addListener((obs, oldVal, newVal) -> {
            txtClienteNombre.setVisible(newVal);
            txtClienteNombre.setManaged(newVal);
            lblClienteLabel.setVisible(newVal);
            lblClienteLabel.setManaged(newVal);
        });
        txtClienteNombre.setVisible(false);
        txtClienteNombre.setManaged(false);
        lblClienteLabel.setVisible(false);
        lblClienteLabel.setManaged(false);

        // Listener para mostrar precio al seleccionar producto
        cmbProducto.setOnAction(e -> actualizarPrecioUnitario());

        // Listener para calcular subtotal en tiempo real
        txtCantidad.textProperty().addListener((obs, oldVal, newVal) -> calcularSubtotalPreview());

        lblTotal.setText("₡0.00");
        cargarVentasHoy();
        cargarFiados();
    }

    private void actualizarPrecioUnitario() {
        Producto p = cmbProducto.getValue();
        if (p != null) {
            lblPrecioUnitario.setText(String.format("$%.2f / %s", p.getPrecioActivo(), p.getUnidadVenta()));
        } else {
            lblPrecioUnitario.setText("—");
        }
        calcularSubtotalPreview();
    }

    private void calcularSubtotalPreview() {
        Producto p = cmbProducto.getValue();
        String cantStr = txtCantidad.getText();
        if (p != null && cantStr != null && !cantStr.isBlank()) {
            try {
                double cant = Double.parseDouble(cantStr.replace(",", "."));
                double sub = cant * p.getPrecioActivo();
                lblSubtotal.setText(String.format("₡%.2f", sub));
                return;
            } catch (NumberFormatException ignored) {
            }
        }
        lblSubtotal.setText("₡0.00");
    }

    @FXML
    private void handleAgregarProducto() {
        Producto p = cmbProducto.getValue();
        if (p == null) {
            mostrarMensaje("Seleccione un producto.", true);
            return;
        }

        String cantStr = txtCantidad.getText();
        if (cantStr == null || cantStr.isBlank()) {
            mostrarMensaje("Ingrese una cantidad.", true);
            return;
        }

        double cantidad;
        try {
            cantidad = Double.parseDouble(cantStr.replace(",", "."));
            if (cantidad <= 0) {
                mostrarMensaje("La cantidad debe ser mayor a 0.", true);
                return;
            }
        } catch (NumberFormatException e) {
            mostrarMensaje("Cantidad no válida.", true);
            return;
        }

        double subtotal = cantidad * p.getPrecioActivo();

        DetalleVenta dv = new DetalleVenta();
        dv.setProductoId(p.getId());
        dv.setNombreProducto(p.getNombre());
        dv.setCantidad(cantidad);
        dv.setTipoUnidad(p.getUnidadVenta().toUpperCase().equals("KG") ? "KG" : "UNIDAD");
        dv.setSubtotal(subtotal);

        detallesCarrito.add(dv);
        totalVenta += subtotal;
        lblTotal.setText(String.format("₡%.2f", totalVenta));

        // Limpiar inputs
        txtCantidad.clear();
        lblSubtotal.setText("₡0.00");
        mostrarMensaje(p.getNombre() + " agregado.", false);
    }

    @FXML
    private void handleQuitarProducto() {
        DetalleVenta selected = tablaDetalle.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarMensaje("Seleccione un producto del carrito.", true);
            return;
        }
        detallesCarrito.remove(selected);
        totalVenta -= selected.getSubtotal();
        if (totalVenta < 0)
            totalVenta = 0;
        lblTotal.setText(String.format("₡%.2f", totalVenta));
        mostrarMensaje("Producto removido.", false);
    }

    @FXML
    private void handleRegistrarVenta() {
        if (detallesCarrito.isEmpty()) {
            mostrarMensaje("El carrito está vacío.", true);
            return;
        }

        String metodoPago = cmbMetodoPago.getValue();
        String estado = chkFiado.isSelected() ? "PENDIENTE" : "COBRADA";
        String clienteNombre = chkFiado.isSelected() ? txtClienteNombre.getText() : null;

        List<DetalleVenta> detalles = new ArrayList<>(detallesCarrito);
        int ventaId = ventaService.registrarVenta(metodoPago, estado, clienteNombre, detalles);

        if (ventaId > 0) {
            mostrarMensaje("✅ Venta #" + ventaId + " registrada. Total: ₡" + String.format("%.2f", totalVenta), false);
            // Limpiar carrito
            detallesCarrito.clear();
            totalVenta = 0;
            lblTotal.setText("₡0.00");
            chkFiado.setSelected(false);
            txtClienteNombre.clear();
            cargarVentasHoy();
            cargarFiados();
        } else {
            mostrarMensaje("Error al registrar la venta.", true);
        }
    }

    @FXML
    private void handleLimpiarCarrito() {
        detallesCarrito.clear();
        totalVenta = 0;
        lblTotal.setText("₡0.00");
        mostrarMensaje("Carrito limpiado.", false);
    }

    @FXML
    private void handleCobrarFiado() {
        Venta selected = tablaFiados.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarMensaje("Seleccione una venta pendiente.", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cobrar Venta Fiada");
        confirm.setHeaderText("¿Cobrar la venta #" + selected.getId() + " de " +
                (selected.getClienteNombre() != null ? selected.getClienteNombre() : "cliente") +
                " por ₡" + String.format("%.2f", selected.getTotal()) + "?");
        Optional<ButtonType> ans = confirm.showAndWait();
        if (ans.isPresent() && ans.get() == ButtonType.OK) {
            String error = ventaService.cobrarVenta(selected.getId());
            if (error == null) {
                mostrarMensaje("Venta #" + selected.getId() + " cobrada.", false);
                cargarVentasHoy();
                cargarFiados();
            } else {
                mostrarMensaje(error, true);
            }
        }
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
        cargarVentasHoy();
        cargarFiados();
        // Recargar productos activos
        cmbProducto.setItems(FXCollections.observableArrayList(productoService.listarActivos()));
        mostrarMensaje("Datos actualizados.", false);
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
