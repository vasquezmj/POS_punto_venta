package com.sellcontrol.dao;

import com.sellcontrol.db.DatabaseManager;
import com.sellcontrol.model.AuditLog;

import java.sql.*;

/**
 * Data Access Object para registros de auditoría.
 */
public class AuditLogDAO {

    /**
     * Registra una acción en el log de auditoría.
     */
    public void insert(AuditLog log) {
        String sql = "INSERT INTO audit_log (usuario_id, accion, entidad, entidad_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, log.getUsuarioId());
            ps.setString(2, log.getAccion());
            ps.setString(3, log.getEntidad());
            if (log.getEntidadId() != null) {
                ps.setInt(4, log.getEntidadId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[AuditLogDAO] Error al registrar auditoría: " + e.getMessage());
        }
    }
}
