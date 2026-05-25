package com.sellcontrol.controller;

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
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para gestionar el carrito y el proceso de cobro de una pestaña de venta individual.
 */
public class VentaTabController {

    @FXML
    private TextField txtBuscarProducto;
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
    private Label lblTotalTabla;
    @FXML
    private ComboBox<String> cmbMetodoPago;
    @FXML
    private CheckBox chkFiado;
    @FXML
    private TextField txtClienteNombre;
    @FXML
    private Label lblClienteLabel;
    @FXML
    private CheckBox chkImprimirTicket;

    private final ProductoService productoService = new ProductoService();
    private final VentaService ventaService = new VentaService();
    private final TicketPrintService ticketPrintService = new TicketPrintService();
    private final ObservableList<DetalleVenta> detallesCarrito = FXCollections.observableArrayList();
    private double totalVenta = 0;

    private VentaController parentController;
    private Tab myTab;

    public void setParentController(VentaController parentController) {
        this.parentController = parentController;
    }

    public void setTab(Tab tab) {
        this.myTab = tab;
    }

    public boolean isCartEmpty() {
        return detallesCarrito.isEmpty();
    }

    @FXML
    public void initialize() {
        // Cargar productos activos en combo
        List<Producto> productosActivos = productoService.listarActivos();
        cmbProducto.setItems(FXCollections.observableArrayList(productosActivos));

        // Listener para búsqueda por ID o Nombre
        txtBuscarProducto.textProperty()
                .addListener((obs, oldVal, newVal) -> filtrarProductos(newVal, productosActivos));

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

        // Listener para mostrar/ocultar campo cliente y método de pago
        chkFiado.selectedProperty().addListener((obs, oldVal, newVal) -> {
            txtClienteNombre.setVisible(newVal);
            txtClienteNombre.setManaged(newVal);
            lblClienteLabel.setVisible(newVal);
            lblClienteLabel.setManaged(newVal);
            // Ocultar método de pago cuando es fiado
            cmbMetodoPago.setDisable(newVal);
        });
        txtClienteNombre.setVisible(false);
        txtClienteNombre.setManaged(false);
        lblClienteLabel.setVisible(false);
        lblClienteLabel.setManaged(false);

        // Listener para mostrar precio al seleccionar producto
        cmbProducto.setOnAction(e -> actualizarPrecioUnitario());

        // Listener para calcular subtotal en tiempo real
        txtCantidad.textProperty().addListener((obs, oldVal, newVal) -> calcularSubtotalPreview());

        if (lblTotalTabla != null)
            lblTotalTabla.setText("₡0.00");
    }

    public void recargarProductos() {
        List<Producto> productosActivos = productoService.listarActivos();
        cmbProducto.setItems(FXCollections.observableArrayList(productosActivos));
        txtBuscarProducto.clear();
    }

    private void filtrarProductos(String filtro, List<Producto> todos) {
        if (filtro == null || filtro.trim().isEmpty()) {
            cmbProducto.setItems(FXCollections.observableArrayList(todos));
            return;
        }
        String f = filtro.toLowerCase().trim();
        List<Producto> filtrados = todos.stream()
                .filter(p -> String.valueOf(p.getId()).startsWith(f) || p.getNombre().toLowerCase().contains(f))
                .toList();

        cmbProducto.setItems(FXCollections.observableArrayList(filtrados));
        if (filtrados.size() == 1) {
            cmbProducto.setValue(filtrados.get(0));
            txtCantidad.requestFocus();
        } else if (!filtrados.isEmpty()) {
            cmbProducto.show();
        }
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
            if (!p.isVentaPorKg() && cantidad != Math.floor(cantidad)) {
                mostrarMensaje("Este producto se vende por unidad. Ingrese un número entero.", true);
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
        if (lblTotalTabla != null)
            lblTotalTabla.setText(String.format("₡%.2f", totalVenta));

        txtBuscarProducto.clear();
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
        if (lblTotalTabla != null)
            lblTotalTabla.setText(String.format("₡%.2f", totalVenta));
        mostrarMensaje("Producto removido.", false);
    }

    @FXML
    private void handleRegistrarVenta() {
        if (detallesCarrito.isEmpty()) {
            mostrarMensaje("El carrito está vacío.", true);
            return;
        }

        boolean esFiado = chkFiado.isSelected();
        String metodoPago = esFiado ? "PENDIENTE" : cmbMetodoPago.getValue();
        String estado = esFiado ? "PENDIENTE" : "COBRADA";
        String clienteNombre = esFiado ? txtClienteNombre.getText() : null;

        double montoPagado = 0;
        double cambio = 0;

        if ("EFECTIVO".equals(metodoPago) && !chkFiado.isSelected()) {
            TextInputDialog dlgPago = new TextInputDialog();
            dlgPago.setTitle("Pago en Efectivo");
            dlgPago.setHeaderText(String.format("Total a cobrar: ₡%.2f", totalVenta));
            dlgPago.setContentText("¿Con cuánto paga el cliente? ₡");

            Optional<String> resultado = dlgPago.showAndWait();
            if (resultado.isEmpty() || resultado.get().isBlank()) {
                mostrarMensaje("Venta cancelada (no se ingresó monto de pago).", true);
                return;
            }

            try {
                montoPagado = Double.parseDouble(resultado.get().replace(",", "."));
            } catch (NumberFormatException e) {
                mostrarMensaje("El monto ingresado no es válido.", true);
                return;
            }

            if (montoPagado < totalVenta) {
                mostrarMensaje(String.format("El monto (₡%.2f) es menor al total (₡%.2f).", montoPagado, totalVenta),
                        true);
                return;
            }

            cambio = montoPagado - totalVenta;

            Dialog<Void> dlgCambio = new Dialog<>();
            dlgCambio.setTitle("💰 Cambio");
            dlgCambio.setHeaderText(null);

            VBox contenido = new VBox(12);
            contenido.setAlignment(Pos.CENTER);
            contenido.setPadding(new Insets(20, 30, 20, 30));
            contenido.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

            Label lblTituloCambio = new Label("✅ Venta registrada");
            lblTituloCambio.setFont(Font.font("System", FontWeight.BOLD, 16));
            lblTituloCambio.setStyle("-fx-text-fill: #2c3e50;");

            Label lblTotalDlg = new Label(String.format("Total: ₡%.2f", totalVenta));
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
        }

        List<DetalleVenta> detalles = new ArrayList<>(detallesCarrito);
        int ventaId = ventaService.registrarVenta(metodoPago, estado, clienteNombre, detalles);

        if (ventaId > 0) {
            mostrarMensaje("✅ Venta #" + ventaId + " registrada. Total: ₡" + String.format("%.2f", totalVenta), false);

            if ("EFECTIVO".equals(metodoPago) && !chkFiado.isSelected()) {
                ticketPrintService.abrirCajon();
            }

            if (chkImprimirTicket != null && chkImprimirTicket.isSelected()) {
                Venta ventaCompleta = ventaService.buscarPorId(ventaId);
                if (ventaCompleta != null) {
                    List<DetalleVenta> detallesImprimir = ventaService.obtenerDetalles(ventaId);
                    String cajero = ventaCompleta.getNombreUsuario() != null ? ventaCompleta.getNombreUsuario() : "—";
                    String errorTicket = ticketPrintService.imprimir(ventaCompleta, detallesImprimir, cajero,
                            montoPagado, cambio);
                    if (errorTicket != null) {
                        mostrarMensaje("Venta registrada, pero error al imprimir: " + errorTicket, true);
                    }
                }
            }

            detallesCarrito.clear();
            totalVenta = 0;
            if (lblTotalTabla != null)
                lblTotalTabla.setText("₡0.00");
            chkFiado.setSelected(false);
            txtClienteNombre.clear();

            if (parentController != null) {
                parentController.recargarVentasYFiados();
                parentController.cerrarTabDespuesDeVenta(myTab);
            }
        } else {
            mostrarMensaje("Error al registrar la venta.", true);
        }
    }

    @FXML
    private void handleLimpiarCarrito() {
        if (detallesCarrito.isEmpty()) {
            mostrarMensaje("El carrito ya está vacío.", false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Limpiar Carrito");
        confirm.setHeaderText("¿Estás seguro de que querés vaciar el carrito?");
        confirm.setContentText("Se perderán todos los productos agregados a la venta actual.");

        Optional<ButtonType> ans = confirm.showAndWait();
        if (ans.isPresent() && ans.get() == ButtonType.OK) {
            detallesCarrito.clear();
            totalVenta = 0;
            if (lblTotalTabla != null)
                lblTotalTabla.setText("₡0.00");
            mostrarMensaje("Carrito limpiado.", false);
        }
    }

    private void mostrarMensaje(String msg, boolean esError) {
        if (parentController != null) {
            parentController.mostrarMensaje(msg, esError);
        }
    }
}
