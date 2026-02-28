package com.sellcontrol.dao;

import com.sellcontrol.db.DatabaseManager;
import com.sellcontrol.model.Gasto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GastoDAO {

    public int insert(Gasto g) {
        String sql = "INSERT INTO gastos (tipo, monto, descripcion) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, g.getTipo());
            ps.setDouble(2, g.getMonto());
            ps.setString(3, g.getDescripcion());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next())
                    return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[GastoDAO] Error en insert: " + e.getMessage());
        }
        return -1;
    }

    public List<Gasto> findHoy() {
        List<Gasto> list = new ArrayList<>();
        String sql = "SELECT * FROM gastos WHERE DATE(fecha_hora) = DATE('now','localtime') ORDER BY fecha_hora DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[GastoDAO] Error en findHoy: " + e.getMessage());
        }
        return list;
    }

    public List<Gasto> findByRango(String desde, String hasta) {
        List<Gasto> list = new ArrayList<>();
        String sql = "SELECT * FROM gastos WHERE DATE(fecha_hora) BETWEEN ? AND ? ORDER BY fecha_hora DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[GastoDAO] Error en findByRango: " + e.getMessage());
        }
        return list;
    }

    private Gasto mapRow(ResultSet rs) throws SQLException {
        Gasto g = new Gasto();
        g.setId(rs.getInt("id"));
        g.setTipo(rs.getString("tipo"));
        g.setMonto(rs.getDouble("monto"));
        g.setFechaHora(rs.getString("fecha_hora"));
        g.setDescripcion(rs.getString("descripcion"));
        return g;
    }
}
