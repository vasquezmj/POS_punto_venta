package com.sellcontrol.service;

import com.sellcontrol.dao.AuditLogDAO;
import com.sellcontrol.dao.UsuarioDAO;
import com.sellcontrol.model.AuditLog;
import com.sellcontrol.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

/**
 * Servicio de gestión de usuarios.
 * Contiene la lógica de negocio para CRUD de usuarios.
 */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    /**
     * Obtiene la lista completa de usuarios.
     */
    public List<Usuario> listarTodos() {
        return usuarioDAO.findAll();
    }

    /**
     * Busca un usuario por ID.
     */
    public Usuario buscarPorId(int id) {
        return usuarioDAO.findById(id);
    }

    /**
     * Crea un nuevo usuario con contraseña hasheada.
     * 
     * @return mensaje de error o null si fue exitoso.
     */
    public String crear(String nombre, String usuario, String password, String rol) {
        // Validaciones
        if (nombre == null || nombre.isBlank())
            return "El nombre es obligatorio.";
        if (usuario == null || usuario.isBlank())
            return "El nombre de usuario es obligatorio.";
        if (password == null || password.length() < 4)
            return "La contraseña debe tener al menos 4 caracteres.";
        if (rol == null || (!rol.equals("ADMIN") && !rol.equals("CAJERO")))
            return "Rol inválido.";

        // Verificar duplicados
        if (usuarioDAO.existsByUsuario(usuario.trim())) {
            return "Ya existe un usuario con ese nombre de usuario.";
        }

        // Crear usuario
        Usuario u = new Usuario();
        u.setNombre(nombre.trim());
        u.setUsuario(usuario.trim());
        u.setContrasena(BCrypt.hashpw(password, BCrypt.gensalt()));
        u.setRol(rol);
        u.setActivo(true);

        int id = usuarioDAO.insert(u);
        if (id > 0) {
            // Auditoría
            Usuario current = AuthService.getCurrentUser();
            if (current != null) {
                auditLogDAO.insert(new AuditLog(current.getId(), "CREAR_USUARIO", "USUARIO", id));
            }
            return null; // éxito
        }

        return "Error al crear el usuario en la base de datos.";
    }

    /**
     * Actualiza los datos de un usuario existente.
     * 
     * @return mensaje de error o null si fue exitoso.
     */
    public String actualizar(int id, String nombre, String usuario, String rol, boolean activo) {
        if (nombre == null || nombre.isBlank())
            return "El nombre es obligatorio.";
        if (usuario == null || usuario.isBlank())
            return "El nombre de usuario es obligatorio.";
        if (rol == null || (!rol.equals("ADMIN") && !rol.equals("CAJERO")))
            return "Rol inválido.";

        // Verificar que el usuario existe
        Usuario existente = usuarioDAO.findById(id);
        if (existente == null)
            return "Usuario no encontrado.";

        // Verificar duplicados (si cambió el nombre de usuario)
        if (!existente.getUsuario().equals(usuario.trim())) {
            if (usuarioDAO.existsByUsuario(usuario.trim())) {
                return "Ya existe un usuario con ese nombre de usuario.";
            }
        }

        existente.setNombre(nombre.trim());
        existente.setUsuario(usuario.trim());
        existente.setRol(rol);
        existente.setActivo(activo);

        if (usuarioDAO.update(existente)) {
            // Auditoría
            Usuario current = AuthService.getCurrentUser();
            if (current != null) {
                auditLogDAO.insert(new AuditLog(current.getId(), "EDITAR_USUARIO", "USUARIO", id));
            }
            return null; // éxito
        }

        return "Error al actualizar el usuario.";
    }

    /**
     * Cambia la contraseña de un usuario.
     * 
     * @return mensaje de error o null si fue exitoso.
     */
    public String cambiarPassword(int id, String newPassword) {
        if (newPassword == null || newPassword.length() < 4) {
            return "La contraseña debe tener al menos 4 caracteres.";
        }

        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        if (usuarioDAO.updatePassword(id, hashed)) {
            Usuario current = AuthService.getCurrentUser();
            if (current != null) {
                auditLogDAO.insert(new AuditLog(current.getId(), "CAMBIAR_PASSWORD", "USUARIO", id));
            }
            return null;
        }

        return "Error al cambiar la contraseña.";
    }
}
