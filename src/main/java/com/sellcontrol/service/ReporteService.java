package com.sellcontrol.service;

import com.sellcontrol.dao.GastoDAO;
import com.sellcontrol.dao.MermaDAO;
import com.sellcontrol.dao.VentaDAO;
import com.sellcontrol.model.Gasto;
import com.sellcontrol.model.Merma;
import com.sellcontrol.model.Venta;

import java.util.List;

/**
 * Servicio de reportes.
 * Consultas agregadas para reportes diarios/semanales y ganancia real.
 */
public class ReporteService {

    private final VentaDAO ventaDAO = new VentaDAO();
    private final GastoDAO gastoDAO = new GastoDAO();
    private final MermaDAO mermaDAO = new MermaDAO();

    /**
     * Obtiene ventas por rango de fechas.
     */
    public List<Venta> ventasPorRango(String desde, String hasta) {
        return ventaDAO.findByRangoFechas(desde, hasta);
    }

    /**
     * Obtiene gastos por rango de fechas.
     */
    public List<Gasto> gastosPorRango(String desde, String hasta) {
        return gastoDAO.findByRango(desde, hasta);
    }

    /**
     * Obtiene mermas por rango de fechas.
     */
    public List<Merma> mermasPorRango(String desde, String hasta) {
        return mermaDAO.findByRango(desde, hasta);
    }

    /**
     * Calcula el total de ventas cobradas en un rango.
     */
    public double totalVentasCobradas(String desde, String hasta) {
        return ventasPorRango(desde, hasta).stream()
                .filter(v -> "COBRADA".equals(v.getEstado()))
                .mapToDouble(Venta::getTotal)
                .sum();
    }

    /**
     * Calcula el total de gastos en un rango.
     */
    public double totalGastos(String desde, String hasta) {
        return gastosPorRango(desde, hasta).stream()
                .mapToDouble(Gasto::getMonto)
                .sum();
    }

    /**
     * Calcula el total de merma en un rango.
     */
    public double totalMerma(String desde, String hasta) {
        return mermasPorRango(desde, hasta).stream()
                .mapToDouble(Merma::getMontoAproximado)
                .sum();
    }

    /**
     * Calcula la ganancia real: Ventas cobradas - Gastos - Merma.
     */
    public double gananciaReal(String desde, String hasta) {
        return totalVentasCobradas(desde, hasta) - totalGastos(desde, hasta) - totalMerma(desde, hasta);
    }

    /**
     * Total de ventas (todas) en un rango.
     */
    public double totalVentasTodas(String desde, String hasta) {
        return ventasPorRango(desde, hasta).stream()
                .mapToDouble(Venta::getTotal)
                .sum();
    }

    /**
     * Total ventas por método de pago.
     */
    public double totalPorMetodoPago(String desde, String hasta, String metodo) {
        return ventasPorRango(desde, hasta).stream()
                .filter(v -> metodo.equals(v.getMetodoPago()) && "COBRADA".equals(v.getEstado()))
                .mapToDouble(Venta::getTotal)
                .sum();
    }
}
