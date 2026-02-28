package com.sellcontrol.dao;

import com.sellcontrol.db.DatabaseManager;
import com.sellcontrol.model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad Producto.
 */
public class ProductoDAO {

    /**
     * Busca un producto por ID.
     */
    public Producto findById(int id) {
        String sql = "SELECT * FROM productos WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ProductoDAO] Error en findById: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retorna todos los productos (activos e inactivos).
     */
    public List<Producto> findAll() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM productos ORDER BY nombre";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                productos.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ProductoDAO] Error en findAll: " + e.getMessage());
        }
        return productos;
    }

    /**
     * Retorna solo los productos activos.
     */
    public List<Producto> findActivos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM productos WHERE activo = 1 ORDER BY nombre";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                productos.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ProductoDAO] Error en findActivos: " + e.getMessage());
        }
        return productos;
    }

    /**
     * Busca productos por nombre (búsqueda parcial, case-insensitive).
     */
    public List<Producto> findByNombre(String nombre) {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM productos WHERE LOWER(nombre) LIKE LOWER(?) ORDER BY nombre";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + nombre + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ProductoDAO] Error en findByNombre: " + e.getMessage());
        }
        return productos;
    }

    /**
     * Inserta un nuevo producto.
     * 
     * @return el ID generado, o -1 si falla.
     */
    public int insert(Producto p) {
        String sql = "INSERT INTO productos (nombre, tipo, precio_por_kg, precio_por_unidad, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getTipo());
            setNullableDouble(ps, 3, p.getPrecioPorKg());
            setNullableDouble(ps, 4, p.getPrecioPorUnidad());
            ps.setInt(5, p.isActivo() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ProductoDAO] Error en insert: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Actualiza un producto existente.
     */
    public boolean update(Producto p) {
        String sql = "UPDATE productos SET nombre = ?, tipo = ?, precio_por_kg = ?, precio_por_unidad = ?, activo = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getTipo());
            setNullableDouble(ps, 3, p.getPrecioPorKg());
            setNullableDouble(ps, 4, p.getPrecioPorUnidad());
            ps.setInt(5, p.isActivo() ? 1 : 0);
            ps.setInt(6, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ProductoDAO] Error en update: " + e.getMessage());
        }
        return false;
    }

    private void setNullableDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        if (value != null) {
            ps.setDouble(index, value);
        } else {
            ps.setNull(index, Types.REAL);
        }
    }

    private Producto mapRow(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setTipo(rs.getString("tipo"));
        double kg = rs.getDouble("precio_por_kg");
        p.setPrecioPorKg(rs.wasNull() ? null : kg);
        double unidad = rs.getDouble("precio_por_unidad");
        p.setPrecioPorUnidad(rs.wasNull() ? null : unidad);
        p.setActivo(rs.getInt("activo") == 1);
        p.setCreadoEn(rs.getString("creado_en"));
        return p;
    }
}
