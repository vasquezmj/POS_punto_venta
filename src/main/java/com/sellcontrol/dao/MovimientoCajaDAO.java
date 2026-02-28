package com.sellcontrol.dao;

import com.sellcontrol.db.DatabaseManager;
import com.sellcontrol.model.MovimientoCaja;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovimientoCajaDAO {

    public int insert(MovimientoCaja m) {
        String sql = "INSERT INTO movimientos_caja (tipo, monto, motivo, usuario_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getTipo());
            ps.setDouble(2, m.getMonto());
            ps.setString(3, m.getMotivo());
            ps.setInt(4, m.getUsuarioId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next())
                    return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[MovimientoCajaDAO] Error en insert: " + e.getMessage());
        }
        return -1;
    }

    public List<MovimientoCaja> findHoy() {
        List<MovimientoCaja> list = new ArrayList<>();
        String sql = "SELECT mc.*, u.nombre AS nombre_usuario FROM movimientos_caja mc JOIN usuarios u ON mc.usuario_id = u.id "
                +
                "WHERE DATE(mc.fecha_hora) = DATE('now','localtime') ORDER BY mc.fecha_hora DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[MovimientoCajaDAO] Error en findHoy: " + e.getMessage());
        }
        return list;
    }

    private MovimientoCaja mapRow(ResultSet rs) throws SQLException {
        MovimientoCaja m = new MovimientoCaja();
        m.setId(rs.getInt("id"));
        m.setTipo(rs.getString("tipo"));
        m.setMonto(rs.getDouble("monto"));
        m.setMotivo(rs.getString("motivo"));
        m.setUsuarioId(rs.getInt("usuario_id"));
        m.setFechaHora(rs.getString("fecha_hora"));
        m.setNombreUsuario(rs.getString("nombre_usuario"));
        return m;
    }
}
