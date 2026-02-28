package com.sellcontrol.controller;

import com.sellcontrol.App;
import com.sellcontrol.model.Producto;
import com.sellcontrol.service.ProductoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.Optional;

/**
 * Controlador para la gestión de productos (CRUD + búsqueda).
 */
public class ProductoController {

    @FXML
    private TableView<Producto> tablaProductos;
    @FXML
    private TableColumn<Producto, Integer> colId;
    @FXML
    private TableColumn<Producto, String> colNombre;
    @FXML
    private TableColumn<Producto, String> colTipo;
    @FXML
    private TableColumn<Producto, String> colUnidad;
    @FXML
    private TableColumn<Producto, String> colPrecio;
    @FXML
    private TableColumn<Producto, String> colActivo;
    @FXML
    private TextField txtBuscar;
    @FXML
    private Label lblMensaje;

    private final ProductoService productoService = new ProductoService();
    private ObservableList<Producto> productos;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colUnidad.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUnidadVenta()));
        colPrecio.setCellValueFactory(
                cellData -> new SimpleStringProperty(String.format("₡%.2f", cellData.getValue().getPrecioActivo())));
        colActivo.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().isActivo() ? "Sí" : "No"));

        cargarDatos();
    }

    private void cargarDatos() {
        productos = FXCollections.observableArrayList(productoService.listarTodos());
        tablaProductos.setItems(productos);
        lblMensaje.setText("Total: " + productos.size() + " productos");
        lblMensaje.setStyle("-fx-text-fill: #7f8c8d;");
    }

    @FXML
    private void handleBuscar() {
        String texto = txtBuscar.getText();
        if (texto == null || texto.isBlank()) {
            cargarDatos();
        } else {
            productos = FXCollections.observableArrayList(productoService.buscarPorNombre(texto));
            tablaProductos.setItems(productos);
            lblMensaje.setText("Resultados: " + productos.size());
            lblMensaje.setStyle("-fx-text-fill: #7f8c8d;");
        }
    }

    @FXML
    private void handleNuevo() {
        Dialog<Producto> dialog = crearDialogoProducto("Nuevo Producto", null);
        Optional<Producto> result = dialog.showAndWait();
        result.ifPresent(p -> {
            String tipoUnidad = p.isVentaPorKg() ? "KG" : "UNIDAD";
            String precioStr = String.valueOf(p.getPrecioActivo());
            String error = productoService.crear(p.getNombre(), p.getTipo(), tipoUnidad, precioStr);
            if (error == null) {
                mostrarMensaje("Producto creado exitosamente.", false);
                cargarDatos();
            } else {
                mostrarMensaje(error, true);
            }
        });
    }

    @FXML
    private void handleEditar() {
        Producto selected = tablaProductos.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarMensaje("Seleccione un producto para editar.", true);
            return;
        }

        Dialog<Producto> dialog = crearDialogoProducto("Editar Producto", selected);
        Optional<Producto> result = dialog.showAndWait();
        result.ifPresent(p -> {
            String tipoUnidad = p.isVentaPorKg() ? "KG" : "UNIDAD";
            String precioStr = String.valueOf(p.getPrecioActivo());
            String error = productoService.actualizar(selected.getId(), p.getNombre(), p.getTipo(),
                    tipoUnidad, precioStr, p.isActivo());
            if (error == null) {
                mostrarMensaje("Producto actualizado exitosamente.", false);
                cargarDatos();
            } else {
                mostrarMensaje(error, true);
            }
        });
    }

    @FXML
    private void handleToggleActivo() {
        Producto selected = tablaProductos.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarMensaje("Seleccione un producto.", true);
            return;
        }

        String accion = selected.isActivo() ? "desactivar" : "activar";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar");
        confirm.setHeaderText("¿Desea " + accion + " el producto \"" + selected.getNombre() + "\"?");
        Optional<ButtonType> ans = confirm.showAndWait();
        if (ans.isPresent() && ans.get() == ButtonType.OK) {
            String error = productoService.toggleActivo(selected.getId());
            if (error == null) {
                mostrarMensaje("Producto " + (selected.isActivo() ? "desactivado" : "activado") + ".", false);
                cargarDatos();
            } else {
                mostrarMensaje(error, true);
            }
        }
    }

    @FXML
    private void handleVolver() {
        App.changeScene("dashboard.fxml", "Panel Principal", 900, 600);
    }

    private Dialog<Producto> crearDialogoProducto(String titulo, Producto existing) {
        Dialog<Producto> dialog = new Dialog<>();
        dialog.setTitle(titulo);

        ButtonType saveButton = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtNombre = new TextField(existing != null ? existing.getNombre() : "");
        txtNombre.setPromptText("Nombre del producto");

        ComboBox<String> cmbTipo = new ComboBox<>(FXCollections.observableArrayList("FRUTA", "VERDURA", "OTRO"));
        cmbTipo.setValue(existing != null ? existing.getTipo() : "VERDURA");

        ToggleGroup tgUnidad = new ToggleGroup();
        RadioButton rbKg = new RadioButton("Por Kg");
        rbKg.setToggleGroup(tgUnidad);
        RadioButton rbUnidad = new RadioButton("Por Unidad");
        rbUnidad.setToggleGroup(tgUnidad);

        if (existing != null && existing.isVentaPorKg()) {
            rbKg.setSelected(true);
        } else if (existing != null) {
            rbUnidad.setSelected(true);
        } else {
            rbKg.setSelected(true);
        }

        TextField txtPrecio = new TextField(existing != null ? String.format("%.2f", existing.getPrecioActivo()) : "");
        txtPrecio.setPromptText("0.00");

        CheckBox chkActivo = new CheckBox("Activo");
        chkActivo.setSelected(existing == null || existing.isActivo());

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Tipo:"), 0, 1);
        grid.add(cmbTipo, 1, 1);
        grid.add(new Label("Venta:"), 0, 2);
        javafx.scene.layout.HBox hbUnidad = new javafx.scene.layout.HBox(10, rbKg, rbUnidad);
        grid.add(hbUnidad, 1, 2);
        grid.add(new Label("Precio:"), 0, 3);
        grid.add(txtPrecio, 1, 3);
        if (existing != null) {
            grid.add(chkActivo, 1, 4);
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                Producto p = new Producto();
                p.setNombre(txtNombre.getText());
                p.setTipo(cmbTipo.getValue());
                p.setActivo(chkActivo.isSelected());

                double precio;
                try {
                    precio = Double.parseDouble(txtPrecio.getText().replace(",", "."));
                } catch (NumberFormatException e) {
                    precio = 0;
                }

                if (rbKg.isSelected()) {
                    p.setPrecioPorKg(precio);
                    p.setPrecioPorUnidad(null);
                } else {
                    p.setPrecioPorKg(null);
                    p.setPrecioPorUnidad(precio);
                }

                return p;
            }
            return null;
        });

        return dialog;
    }

    private void mostrarMensaje(String msg, boolean esError) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(esError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}
