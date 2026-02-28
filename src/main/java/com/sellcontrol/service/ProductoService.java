package com.sellcontrol.service;

import com.sellcontrol.dao.AuditLogDAO;
import com.sellcontrol.dao.ProductoDAO;
import com.sellcontrol.model.AuditLog;
import com.sellcontrol.model.Producto;
import com.sellcontrol.model.Usuario;

import java.util.List;

/**
 * Servicio de gestión de productos.
 * Contiene la lógica de negocio para CRUD de productos.
 */
public class ProductoService {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    /**
     * Obtiene todos los productos.
     */
    public List<Producto> listarTodos() {
        return productoDAO.findAll();
    }

    /**
     * Obtiene solo los productos activos.
     */
    public List<Producto> listarActivos() {
        return productoDAO.findActivos();
    }

    /**
     * Busca productos por nombre (parcial).
     */
    public List<Producto> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return productoDAO.findAll();
        }
        return productoDAO.findByNombre(nombre.trim());
    }

    /**
     * Busca un producto por ID.
     */
    public Producto buscarPorId(int id) {
        return productoDAO.findById(id);
    }

    /**
     * Crea un nuevo producto.
     * 
     * @return mensaje de error o null si fue exitoso.
     */
    public String crear(String nombre, String tipo, String tipoUnidad, String precioStr) {
        // Validaciones
        if (nombre == null || nombre.isBlank())
            return "El nombre es obligatorio.";
        if (tipo == null)
            return "El tipo es obligatorio.";
        if (tipoUnidad == null)
            return "Debe indicar si se vende por Kg o Unidad.";
        if (precioStr == null || precioStr.isBlank())
            return "El precio es obligatorio.";

        double precio;
        try {
            precio = Double.parseDouble(precioStr.replace(",", "."));
            if (precio <= 0)
                return "El precio debe ser mayor a 0.";
        } catch (NumberFormatException e) {
            return "El precio no es un número válido.";
        }

        Producto p = new Producto();
        p.setNombre(nombre.trim());
        p.setTipo(tipo);
        p.setActivo(true);

        if ("KG".equals(tipoUnidad)) {
            p.setPrecioPorKg(precio);
            p.setPrecioPorUnidad(null);
        } else {
            p.setPrecioPorKg(null);
            p.setPrecioPorUnidad(precio);
        }

        int id = productoDAO.insert(p);
        if (id > 0) {
            registrarAuditoria("CREAR_PRODUCTO", id);
            return null;
        }

        return "Error al crear el producto en la base de datos.";
    }

    /**
     * Actualiza un producto existente.
     * 
     * @return mensaje de error o null si fue exitoso.
     */
    public String actualizar(int id, String nombre, String tipo, String tipoUnidad, String precioStr, boolean activo) {
        if (nombre == null || nombre.isBlank())
            return "El nombre es obligatorio.";
        if (tipo == null)
            return "El tipo es obligatorio.";
        if (tipoUnidad == null)
            return "Debe indicar si se vende por Kg o Unidad.";
        if (precioStr == null || precioStr.isBlank())
            return "El precio es obligatorio.";

        double precio;
        try {
            precio = Double.parseDouble(precioStr.replace(",", "."));
            if (precio <= 0)
                return "El precio debe ser mayor a 0.";
        } catch (NumberFormatException e) {
            return "El precio no es un número válido.";
        }

        Producto p = productoDAO.findById(id);
        if (p == null)
            return "Producto no encontrado.";

        p.setNombre(nombre.trim());
        p.setTipo(tipo);
        p.setActivo(activo);

        if ("KG".equals(tipoUnidad)) {
            p.setPrecioPorKg(precio);
            p.setPrecioPorUnidad(null);
        } else {
            p.setPrecioPorKg(null);
            p.setPrecioPorUnidad(precio);
        }

        if (productoDAO.update(p)) {
            registrarAuditoria("EDITAR_PRODUCTO", id);
            return null;
        }

        return "Error al actualizar el producto.";
    }

    /**
     * Cambia el estado activo/inactivo de un producto.
     */
    public String toggleActivo(int id) {
        Producto p = productoDAO.findById(id);
        if (p == null)
            return "Producto no encontrado.";

        p.setActivo(!p.isActivo());
        if (productoDAO.update(p)) {
            registrarAuditoria(p.isActivo() ? "ACTIVAR_PRODUCTO" : "DESACTIVAR_PRODUCTO", id);
            return null;
        }
        return "Error al cambiar estado del producto.";
    }

    private void registrarAuditoria(String accion, int entidadId) {
        Usuario current = AuthService.getCurrentUser();
        if (current != null) {
            auditLogDAO.insert(new AuditLog(current.getId(), accion, "PRODUCTO", entidadId));
        }
    }
}
