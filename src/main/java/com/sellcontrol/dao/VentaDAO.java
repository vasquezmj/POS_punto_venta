package com.sellcontrol.dao;

import com.sellcontrol.db.DatabaseManager;
import com.sellcontrol.model.Venta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad Venta.
 */
public class VentaDAO {

    /**
     * Inserta una nueva venta y retorna el ID generado.
     */
    public int insert(Venta v) {
        String sql = "INSERT INTO ventas (usuario_id, total, metodo_pago, estado, cliente_nombre) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getUsuarioId());
            ps.setDouble(2, v.getTotal());
            ps.setString(3, v.getMetodoPago());
            ps.setString(4, v.getEstado());
            if (v.getClienteNombre() != null && !v.getClienteNombre().isBlank()) {
                ps.setString(5, v.getClienteNombre());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next())
                    return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[VentaDAO] Error en insert: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Busca una venta por ID.
     */
    public Venta findById(int id) {
        String sql = "SELECT v.*, u.nombre AS nombre_usuario FROM ventas v JOIN usuarios u ON v.usuario_id = u.id WHERE v.id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[VentaDAO] Error en findById: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retorna todas las ventas del día actual.
     */
    public List<Venta> findVentasHoy() {
        List<Venta> ventas = new ArrayList<>();
        String sql = "SELECT v.*, u.nombre AS nombre_usuario FROM ventas v JOIN usuarios u ON v.usuario_id = u.id " +
                "WHERE DATE(v.fecha_hora) = DATE('now','localtime') ORDER BY v.fecha_hora DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                ventas.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[VentaDAO] Error en findVentasHoy: " + e.getMessage());
        }
        return ventas;
    }

    /**
     * Retorna las ventas pendientes (fiadas).
     */
    public List<Venta> findPendientes() {
        List<Venta> ventas = new ArrayList<>();
        String sql = "SELECT v.*, u.nombre AS nombre_usuario FROM ventas v JOIN usuarios u ON v.usuario_id = u.id " +
                "WHERE v.estado = 'PENDIENTE' ORDER BY v.fecha_hora DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                ventas.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[VentaDAO] Error en findPendientes: " + e.getMessage());
        }
        return ventas;
    }

    /**
     * Marca una venta pendiente como cobrada.
     */
    public boolean cobrar(int ventaId) {
        String sql = "UPDATE ventas SET estado = 'COBRADA' WHERE id = ? AND estado = 'PENDIENTE'";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ventaId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[VentaDAO] Error en cobrar: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retorna ventas por rango de fechas.
     */
    public List<Venta> findByRangoFechas(String desde, String hasta) {
        List<Venta> ventas = new ArrayList<>();
        String sql = "SELECT v.*, u.nombre AS nombre_usuario FROM ventas v JOIN usuarios u ON v.usuario_id = u.id " +
                "WHERE DATE(v.fecha_hora) BETWEEN ? AND ? ORDER BY v.fecha_hora DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    ventas.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[VentaDAO] Error en findByRangoFechas: " + e.getMessage());
        }
        return ventas;
    }

    private Venta mapRow(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setId(rs.getInt("id"));
        v.setFechaHora(rs.getString("fecha_hora"));
        v.setUsuarioId(rs.getInt("usuario_id"));
        v.setTotal(rs.getDouble("total"));
        v.setMetodoPago(rs.getString("metodo_pago"));
        v.setEstado(rs.getString("estado"));
        v.setClienteNombre(rs.getString("cliente_nombre"));
        v.setNombreUsuario(rs.getString("nombre_usuario"));
        return v;
    }
}
