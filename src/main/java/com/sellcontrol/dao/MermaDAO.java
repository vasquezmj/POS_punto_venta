package com.sellcontrol.dao;

import com.sellcontrol.db.DatabaseManager;
import com.sellcontrol.model.Merma;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MermaDAO {

    public int insert(Merma m) {
        String sql = "INSERT INTO mermas (descripcion, monto_aproximado) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getDescripcion());
            ps.setDouble(2, m.getMontoAproximado());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next())
                    return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[MermaDAO] Error en insert: " + e.getMessage());
        }
        return -1;
    }

    public List<Merma> findHoy() {
        List<Merma> list = new ArrayList<>();
        String sql = "SELECT * FROM mermas WHERE DATE(fecha_hora) = DATE('now','localtime') ORDER BY fecha_hora DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[MermaDAO] Error en findHoy: " + e.getMessage());
        }
        return list;
    }

    public List<Merma> findByRango(String desde, String hasta) {
        List<Merma> list = new ArrayList<>();
        String sql = "SELECT * FROM mermas WHERE DATE(fecha_hora) BETWEEN ? AND ? ORDER BY fecha_hora DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[MermaDAO] Error en findByRango: " + e.getMessage());
        }
        return list;
    }

    private Merma mapRow(ResultSet rs) throws SQLException {
        Merma m = new Merma();
        m.setId(rs.getInt("id"));
        m.setDescripcion(rs.getString("descripcion"));
        m.setMontoAproximado(rs.getDouble("monto_aproximado"));
        m.setFechaHora(rs.getString("fecha_hora"));
        return m;
    }
}
