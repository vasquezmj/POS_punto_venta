package com.sellcontrol.service;

import com.sellcontrol.dao.AuditLogDAO;
import com.sellcontrol.dao.DetalleVentaDAO;
import com.sellcontrol.dao.VentaDAO;
import com.sellcontrol.model.AuditLog;
import com.sellcontrol.model.DetalleVenta;
import com.sellcontrol.model.Usuario;
import com.sellcontrol.model.Venta;

import java.util.List;

/**
 * Servicio de gestión de ventas.
 * Contiene la lógica de negocio: crear venta con detalles, cobrar fiados,
 * consultar.
 */
public class VentaService {

    private final VentaDAO ventaDAO = new VentaDAO();
    private final DetalleVentaDAO detalleVentaDAO = new DetalleVentaDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    /**
     * Registra una venta completa con sus detalles.
     * 
     * @return el ID de la venta creada, o -1 si falla.
     */
    public int registrarVenta(String metodoPago, String estado, String clienteNombre, List<DetalleVenta> detalles) {
        if (detalles == null || detalles.isEmpty())
            return -1;

        Usuario user = AuthService.getCurrentUser();
        if (user == null)
            return -1;

        // Calcular total
        double total = 0;
        for (DetalleVenta dv : detalles) {
            total += dv.getSubtotal();
        }

        // Crear venta
        Venta v = new Venta();
        v.setUsuarioId(user.getId());
        v.setTotal(total);
        v.setMetodoPago(metodoPago);
        v.setEstado(estado);
        v.setClienteNombre(clienteNombre);

        int ventaId = ventaDAO.insert(v);
        if (ventaId < 0)
            return -1;

        // Insertar detalles
        for (DetalleVenta dv : detalles) {
            dv.setVentaId(ventaId);
            detalleVentaDAO.insert(dv);
        }

        // Auditoría
        auditLogDAO.insert(new AuditLog(user.getId(), "REGISTRAR_VENTA", "VENTA", ventaId));
        System.out.println("[VentaService] Venta #" + ventaId + " registrada. Total: $" + String.format("%.2f", total));

        return ventaId;
    }

    /**
     * Cobra una venta pendiente (fiada).
     */
    public String cobrarVenta(int ventaId) {
        if (ventaDAO.cobrar(ventaId)) {
            Usuario user = AuthService.getCurrentUser();
            if (user != null) {
                auditLogDAO.insert(new AuditLog(user.getId(), "COBRAR_VENTA", "VENTA", ventaId));
            }
            return null;
        }
        return "Error al cobrar la venta.";
    }

    /**
     * Obtiene las ventas del día.
     */
    public List<Venta> ventasDeHoy() {
        return ventaDAO.findVentasHoy();
    }

    /**
     * Obtiene las ventas pendientes (fiadas).
     */
    public List<Venta> ventasPendientes() {
        return ventaDAO.findPendientes();
    }

    /**
     * Obtiene los detalles de una venta.
     */
    public List<DetalleVenta> obtenerDetalles(int ventaId) {
        return detalleVentaDAO.findByVentaId(ventaId);
    }

    /**
     * Obtiene ventas por rango de fechas.
     */
    public List<Venta> ventasPorRango(String desde, String hasta) {
        return ventaDAO.findByRangoFechas(desde, hasta);
    }
}
