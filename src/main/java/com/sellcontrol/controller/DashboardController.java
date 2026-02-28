package com.sellcontrol.controller;

import com.sellcontrol.App;
import com.sellcontrol.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Controlador del Dashboard principal.
 * Muestra opciones según el rol del usuario logueado.
 */
public class DashboardController {

    @FXML
    private Label lblBienvenida;
    @FXML
    private Label lblRol;
    @FXML
    private Button btnUsuarios;
    @FXML
    private Button btnProductos;
    @FXML
    private Button btnVentas;
    @FXML
    private Button btnCaja;
    @FXML
    private Button btnGastos;
    @FXML
    private Button btnReportes;

    @FXML
    public void initialize() {
        var user = AuthService.getCurrentUser();
        if (user != null) {
            lblBienvenida.setText("Bienvenido, " + user.getNombre());
            lblRol.setText("Rol: " + user.getRol());

            // Cajeros solo ven Ventas y Caja
            if (!AuthService.isAdmin()) {
                btnUsuarios.setVisible(false);
                btnUsuarios.setManaged(false);
                btnProductos.setVisible(false);
                btnProductos.setManaged(false);
                btnGastos.setVisible(false);
                btnGastos.setManaged(false);
                btnReportes.setVisible(false);
                btnReportes.setManaged(false);
            }
        }
    }

    @FXML
    private void handleUsuarios() {
        App.changeScene("usuarios.fxml", "Gestión de Usuarios", 850, 550);
    }

    @FXML
    private void handleProductos() {
        App.changeScene("productos.fxml", "Gestión de Productos", 850, 550);
    }

    @FXML
    private void handleVentas() {
        App.changeScene("ventas.fxml", "Módulo de Ventas", 1000, 650);
    }

    @FXML
    private void handleCaja() {
        App.changeScene("caja.fxml", "Control de Caja", 900, 550);
    }

    @FXML
    private void handleGastos() {
        App.changeScene("gastos.fxml", "Gastos y Merma", 950, 600);
    }

    @FXML
    private void handleReportes() {
        App.changeScene("reportes.fxml", "Reportes", 1100, 700);
    }

    @FXML
    private void handleCerrarSesion() {
        new AuthService().logout();
        App.goToLogin();
    }
}
