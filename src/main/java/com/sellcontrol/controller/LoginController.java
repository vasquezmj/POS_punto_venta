package com.sellcontrol.controller;

import com.sellcontrol.App;
import com.sellcontrol.model.Usuario;
import com.sellcontrol.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controlador de la pantalla de Login.
 */
public class LoginController {

    @FXML
    private TextField txtUsuario;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblError;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        lblError.setText("");
    }

    /**
     * Maneja el evento del botón "Iniciar Sesión".
     */
    @FXML
    private void handleLogin() {
        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        if (usuario.isBlank() || password.isBlank()) {
            lblError.setText("Ingrese usuario y contraseña.");
            return;
        }

        Usuario u = authService.login(usuario, password);
        if (u != null) {
            // Login exitoso → ir al Dashboard
            App.changeScene("dashboard.fxml", "Panel Principal", 900, 600);
        } else {
            lblError.setText("Credenciales inválidas. Intente de nuevo.");
            txtPassword.clear();
            txtPassword.requestFocus();
        }
    }
}
