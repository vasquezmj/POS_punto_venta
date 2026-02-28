package com.sellcontrol.service;

import com.sellcontrol.dao.AuditLogDAO;
import com.sellcontrol.dao.UsuarioDAO;
import com.sellcontrol.model.AuditLog;
import com.sellcontrol.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Servicio de autenticación.
 * Maneja login con validación BCrypt y registra auditoría.
 */
public class AuthService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    /** Usuario actualmente logueado en la sesión */
    private static Usuario currentUser;

    /**
     * Intenta autenticar al usuario.
     * 
     * @return el Usuario si las credenciales son válidas, null si no.
     */
    public Usuario login(String usuario, String password) {
        if (usuario == null || usuario.isBlank() || password == null || password.isBlank()) {
            return null;
        }

        Usuario u = usuarioDAO.findByUsuario(usuario.trim());
        if (u == null) {
            return null;
        }

        // Verificar contraseña con BCrypt
        if (BCrypt.checkpw(password, u.getContrasena())) {
            currentUser = u;
            // Registrar auditoría de login
            auditLogDAO.insert(new AuditLog(u.getId(), "LOGIN", "USUARIO", u.getId()));
            System.out.println("[Auth] Login exitoso: " + u.getUsuario() + " (" + u.getRol() + ")");
            return u;
        }

        return null;
    }

    /**
     * Cierra la sesión actual.
     */
    public void logout() {
        if (currentUser != null) {
            auditLogDAO.insert(new AuditLog(currentUser.getId(), "LOGOUT", "USUARIO", currentUser.getId()));
            System.out.println("[Auth] Logout: " + currentUser.getUsuario());
            currentUser = null;
        }
    }

    /**
     * @return el usuario actualmente logueado, o null si no hay sesión.
     */
    public static Usuario getCurrentUser() {
        return currentUser;
    }

    /**
     * @return true si el usuario actual es ADMIN.
     */
    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRol());
    }
}
