package com.sellcontrol.service;

import com.sellcontrol.dao.AuditLogDAO;
import com.sellcontrol.dao.MovimientoCajaDAO;
import com.sellcontrol.model.AuditLog;
import com.sellcontrol.model.MovimientoCaja;
import com.sellcontrol.model.Usuario;

import java.util.List;

/**
 * Servicio de gestión de caja.
 */
public class CajaService {

    private final MovimientoCajaDAO movimientoCajaDAO = new MovimientoCajaDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    public String registrarMovimiento(String tipo, String montoStr, String motivo) {
        if (tipo == null)
            return "Seleccione un tipo de movimiento.";
        if (montoStr == null || montoStr.isBlank())
            return "El monto es obligatorio.";
        if (motivo == null || motivo.isBlank())
            return "El motivo es obligatorio.";

        double monto;
        try {
            monto = Double.parseDouble(montoStr.replace(",", "."));
            if (monto <= 0)
                return "El monto debe ser mayor a 0.";
        } catch (NumberFormatException e) {
            return "Monto no válido.";
        }

        Usuario user = AuthService.getCurrentUser();
        if (user == null)
            return "No hay sesión activa.";

        MovimientoCaja m = new MovimientoCaja();
        m.setTipo(tipo);
        m.setMonto(monto);
        m.setMotivo(motivo.trim());
        m.setUsuarioId(user.getId());

        int id = movimientoCajaDAO.insert(m);
        if (id > 0) {
            auditLogDAO.insert(new AuditLog(user.getId(), "MOVIMIENTO_CAJA", "MOVIMIENTO_CAJA", id));
            return null;
        }
        return "Error al registrar movimiento.";
    }

    public List<MovimientoCaja> movimientosDeHoy() {
        return movimientoCajaDAO.findHoy();
    }
}
