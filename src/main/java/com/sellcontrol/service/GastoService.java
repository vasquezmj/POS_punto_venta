package com.sellcontrol.service;

import com.sellcontrol.dao.GastoDAO;
import com.sellcontrol.dao.MermaDAO;
import com.sellcontrol.model.Gasto;
import com.sellcontrol.model.Merma;

import java.util.List;

/**
 * Servicio de gestión de gastos y merma.
 */
public class GastoService {

    private final GastoDAO gastoDAO = new GastoDAO();
    private final MermaDAO mermaDAO = new MermaDAO();

    public String registrarGasto(String tipo, String montoStr, String descripcion) {
        if (tipo == null)
            return "Seleccione un tipo de gasto.";
        if (montoStr == null || montoStr.isBlank())
            return "El monto es obligatorio.";

        double monto;
        try {
            monto = Double.parseDouble(montoStr.replace(",", "."));
            if (monto <= 0)
                return "El monto debe ser mayor a 0.";
        } catch (NumberFormatException e) {
            return "Monto no válido.";
        }

        Gasto g = new Gasto();
        g.setTipo(tipo);
        g.setMonto(monto);
        g.setDescripcion(descripcion != null ? descripcion.trim() : "");

        return gastoDAO.insert(g) > 0 ? null : "Error al registrar gasto.";
    }

    public String registrarMerma(String descripcion, String montoStr) {
        if (descripcion == null || descripcion.isBlank())
            return "La descripción es obligatoria.";
        if (montoStr == null || montoStr.isBlank())
            return "El monto aproximado es obligatorio.";

        double monto;
        try {
            monto = Double.parseDouble(montoStr.replace(",", "."));
            if (monto <= 0)
                return "El monto debe ser mayor a 0.";
        } catch (NumberFormatException e) {
            return "Monto no válido.";
        }

        Merma m = new Merma();
        m.setDescripcion(descripcion.trim());
        m.setMontoAproximado(monto);

        return mermaDAO.insert(m) > 0 ? null : "Error al registrar merma.";
    }

    public List<Gasto> gastosDeHoy() {
        return gastoDAO.findHoy();
    }

    public List<Merma> mermasDeHoy() {
        return mermaDAO.findHoy();
    }

    public List<Gasto> gastosPorRango(String desde, String hasta) {
        return gastoDAO.findByRango(desde, hasta);
    }

    public List<Merma> mermasPorRango(String desde, String hasta) {
        return mermaDAO.findByRango(desde, hasta);
    }
}
