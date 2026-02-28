package com.sellcontrol.controller;

import com.sellcontrol.App;
import com.sellcontrol.model.Usuario;
import com.sellcontrol.service.UsuarioService;
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
 * Controlador para la gestión de usuarios (CRUD).
 * Solo accesible por ADMIN.
 */
public class UsuarioController {

    @FXML
    private TableView<Usuario> tablaUsuarios;
    @FXML
    private TableColumn<Usuario, Integer> colId;
    @FXML
    private TableColumn<Usuario, String> colNombre;
    @FXML
    private TableColumn<Usuario, String> colUsuario;
    @FXML
    private TableColumn<Usuario, String> colRol;
    @FXML
    private TableColumn<Usuario, String> colActivo;
    @FXML
    private TableColumn<Usuario, String> colCreado;
    @FXML
    private Label lblMensaje;

    private final UsuarioService usuarioService = new UsuarioService();
    private ObservableList<Usuario> usuarios;

    @FXML
    public void initialize() {
        // Configurar columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colActivo.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().isActivo() ? "Sí" : "No"));
        colCreado.setCellValueFactory(new PropertyValueFactory<>("creadoEn"));

        cargarDatos();
    }

    /**
     * Carga la lista de usuarios en la tabla.
     */
    private void cargarDatos() {
        usuarios = FXCollections.observableArrayList(usuarioService.listarTodos());
        tablaUsuarios.setItems(usuarios);
        lblMensaje.setText("Total: " + usuarios.size() + " usuarios");
    }

    /**
     * Muestra un diálogo para crear un nuevo usuario.
     */
    @FXML
    private void handleNuevo() {
        Dialog<Usuario> dialog = crearDialogoUsuario("Nuevo Usuario", null);
        Optional<Usuario> result = dialog.showAndWait();
        result.ifPresent(u -> {
            String error = usuarioService.crear(u.getNombre(), u.getUsuario(), u.getContrasena(), u.getRol());
            if (error == null) {
                mostrarMensaje("Usuario creado exitosamente.", false);
                cargarDatos();
            } else {
                mostrarMensaje(error, true);
            }
        });
    }

    /**
     * Muestra un diálogo para editar el usuario seleccionado.
     */
    @FXML
    private void handleEditar() {
        Usuario selected = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarMensaje("Seleccione un usuario para editar.", true);
            return;
        }

        Dialog<Usuario> dialog = crearDialogoUsuario("Editar Usuario", selected);
        Optional<Usuario> result = dialog.showAndWait();
        result.ifPresent(u -> {
            String error = usuarioService.actualizar(selected.getId(), u.getNombre(), u.getUsuario(), u.getRol(),
                    u.isActivo());
            if (error == null) {
                mostrarMensaje("Usuario actualizado exitosamente.", false);
                cargarDatos();
            } else {
                mostrarMensaje(error, true);
            }
        });
    }

    /**
     * Cambia la contraseña del usuario seleccionado.
     */
    @FXML
    private void handleCambiarPassword() {
        Usuario selected = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarMensaje("Seleccione un usuario.", true);
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cambiar Contraseña");
        dialog.setHeaderText("Nueva contraseña para: " + selected.getNombre());
        dialog.setContentText("Contraseña:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(pwd -> {
            String error = usuarioService.cambiarPassword(selected.getId(), pwd);
            if (error == null) {
                mostrarMensaje("Contraseña actualizada.", false);
            } else {
                mostrarMensaje(error, true);
            }
        });
    }

    /**
     * Activa/desactiva el usuario seleccionado.
     */
    @FXML
    private void handleToggleActivo() {
        Usuario selected = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarMensaje("Seleccione un usuario.", true);
            return;
        }

        boolean nuevoEstado = !selected.isActivo();
        String accion = nuevoEstado ? "activar" : "desactivar";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar");
        confirm.setHeaderText("¿Desea " + accion + " al usuario " + selected.getNombre() + "?");
        Optional<ButtonType> ans = confirm.showAndWait();
        if (ans.isPresent() && ans.get() == ButtonType.OK) {
            String error = usuarioService.actualizar(selected.getId(), selected.getNombre(),
                    selected.getUsuario(), selected.getRol(), nuevoEstado);
            if (error == null) {
                mostrarMensaje("Usuario " + (nuevoEstado ? "activado" : "desactivado") + ".", false);
                cargarDatos();
            } else {
                mostrarMensaje(error, true);
            }
        }
    }

    /**
     * Regresa al Dashboard.
     */
    @FXML
    private void handleVolver() {
        App.changeScene("dashboard.fxml", "Panel Principal", 900, 600);
    }

    /**
     * Crea un diálogo modal para editar/crear usuario.
     */
    private Dialog<Usuario> crearDialogoUsuario(String titulo, Usuario existing) {
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle(titulo);

        ButtonType saveButton = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtNombre = new TextField(existing != null ? existing.getNombre() : "");
        txtNombre.setPromptText("Nombre completo");
        TextField txtUsuario = new TextField(existing != null ? existing.getUsuario() : "");
        txtUsuario.setPromptText("nombre.usuario");
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText(existing != null ? "(dejar vacío para no cambiar)" : "Contraseña");
        ComboBox<String> cmbRol = new ComboBox<>(FXCollections.observableArrayList("ADMIN", "CAJERO"));
        cmbRol.setValue(existing != null ? existing.getRol() : "CAJERO");
        CheckBox chkActivo = new CheckBox("Activo");
        chkActivo.setSelected(existing == null || existing.isActivo());

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Usuario:"), 0, 1);
        grid.add(txtUsuario, 1, 1);
        if (existing == null) {
            grid.add(new Label("Contraseña:"), 0, 2);
            grid.add(txtPassword, 1, 2);
        }
        grid.add(new Label("Rol:"), 0, 3);
        grid.add(cmbRol, 1, 3);
        if (existing != null) {
            grid.add(chkActivo, 1, 4);
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                Usuario u = new Usuario();
                u.setNombre(txtNombre.getText());
                u.setUsuario(txtUsuario.getText());
                u.setContrasena(txtPassword.getText());
                u.setRol(cmbRol.getValue());
                u.setActivo(chkActivo.isSelected());
                return u;
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
