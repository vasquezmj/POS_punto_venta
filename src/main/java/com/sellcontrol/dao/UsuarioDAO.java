package com.sellcontrol.dao;

import com.sellcontrol.db.DatabaseManager;
import com.sellcontrol.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad Usuario.
 * Toda operación de persistencia de usuarios pasa por aquí.
 */
public class UsuarioDAO {

    /**
     * Busca un usuario por su nombre de usuario (login).
     * Solo retorna usuarios activos.
     */
    public Usuario findByUsuario(String usuario) {
        String sql = "SELECT * FROM usuarios WHERE usuario = ? AND activo = 1";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en findByUsuario: " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca un usuario por ID.
     */
    public Usuario findById(int id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en findById: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retorna todos los usuarios (activos e inactivos).
     */
    public List<Usuario> findAll() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY id";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                usuarios.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en findAll: " + e.getMessage());
        }
        return usuarios;
    }

    /**
     * Inserta un nuevo usuario.
     * @return el ID generado, o -1 si falla.
     */
    public int insert(Usuario u) {
        String sql = "INSERT INTO usuarios (nombre, usuario, contrasena, rol, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getUsuario());
            ps.setString(3, u.getContrasena());
            ps.setString(4, u.getRol());
            ps.setInt(5, u.isActivo() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en insert: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Actualiza nombre, rol y estado activo de un usuario existente.
     * No actualiza la contraseña (usar updatePassword para eso).
     */
    public boolean update(Usuario u) {
        String sql = "UPDATE usuarios SET nombre = ?, usuario = ?, rol = ?, activo = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getUsuario());
            ps.setString(3, u.getRol());
            ps.setInt(4, u.isActivo() ? 1 : 0);
            ps.setInt(5, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en update: " + e.getMessage());
        }
        return false;
    }

    /**
     * Actualiza la contraseña de un usuario (ya hasheada).
     */
    public boolean updatePassword(int id, String hashedPassword) {
        String sql = "UPDATE usuarios SET contrasena = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en updatePassword: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica si ya existe un nombre de usuario (para evitar duplicados).
     */
    public boolean existsByUsuario(String usuario) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE usuario = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en existsByUsuario: " + e.getMessage());
        }
        return false;
    }

    /**
     * Mapea un ResultSet a un objeto Usuario.
     */
    private Usuario mapRow(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setUsuario(rs.getString("usuario"));
        u.setContrasena(rs.getString("contrasena"));
        u.setRol(rs.getString("rol"));
        u.setActivo(rs.getInt("activo") == 1);
        u.setCreadoEn(rs.getString("creado_en"));
        return u;
    }
}
