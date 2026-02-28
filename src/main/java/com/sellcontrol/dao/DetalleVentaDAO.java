package com.sellcontrol.dao;

import com.sellcontrol.db.DatabaseManager;
import com.sellcontrol.model.DetalleVenta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad DetalleVenta.
 */
public class DetalleVentaDAO {

    /**
     * Inserta un detalle de venta.
     */
    public int insert(DetalleVenta dv) {
        String sql = "INSERT INTO detalle_venta (venta_id, producto_id, cantidad, tipo_unidad, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, dv.getVentaId());
            ps.setInt(2, dv.getProductoId());
            ps.setDouble(3, dv.getCantidad());
            ps.setString(4, dv.getTipoUnidad());
            ps.setDouble(5, dv.getSubtotal());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next())
                    return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[DetalleVentaDAO] Error en insert: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Retorna los detalles de una venta específica.
     */
    public List<DetalleVenta> findByVentaId(int ventaId) {
        List<DetalleVenta> detalles = new ArrayList<>();
        String sql = "SELECT dv.*, p.nombre AS nombre_producto FROM detalle_venta dv " +
                "JOIN productos p ON dv.producto_id = p.id WHERE dv.venta_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleVenta dv = new DetalleVenta();
                    dv.setId(rs.getInt("id"));
                    dv.setVentaId(rs.getInt("venta_id"));
                    dv.setProductoId(rs.getInt("producto_id"));
                    dv.setCantidad(rs.getDouble("cantidad"));
                    dv.setTipoUnidad(rs.getString("tipo_unidad"));
                    dv.setSubtotal(rs.getDouble("subtotal"));
                    dv.setNombreProducto(rs.getString("nombre_producto"));
                    detalles.add(dv);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DetalleVentaDAO] Error en findByVentaId: " + e.getMessage());
        }
        return detalles;
    }
}
