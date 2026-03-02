package com.sellcontrol.service;

import com.sellcontrol.model.Gasto;
import com.sellcontrol.model.Merma;
import com.sellcontrol.model.Venta;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Servicio de exportación de reportes a Excel (.xlsx).
 * Genera un archivo con hojas de: Resumen, Ventas, Gastos y Merma.
 */
public class ExcelExportService {

    private final ReporteService reporteService = new ReporteService();

    /**
     * Exporta un reporte completo al archivo destino.
     *
     * @param desde   fecha inicio (yyyy-MM-dd)
     * @param hasta   fecha fin (yyyy-MM-dd)
     * @param destino archivo .xlsx de salida
     * @return null si OK, o mensaje de error
     */
    public String exportar(String desde, String hasta, File destino) {
        try (Workbook wb = new XSSFWorkbook()) {

            // Estilos
            CellStyle headerStyle = crearEstiloEncabezado(wb);
            CellStyle moneyStyle = crearEstiloMoneda(wb);
            CellStyle boldStyle = wb.createCellStyle();
            Font boldFont = wb.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            // Datos
            List<Venta> ventas = reporteService.ventasPorRango(desde, hasta);
            List<Gasto> gastos = reporteService.gastosPorRango(desde, hasta);
            List<Merma> mermas = reporteService.mermasPorRango(desde, hasta);

            double totalVentas = reporteService.totalVentasTodas(desde, hasta);
            double cobradas = reporteService.totalVentasCobradas(desde, hasta);
            double pendientes = totalVentas - cobradas;
            double efectivo = reporteService.totalPorMetodoPago(desde, hasta, "EFECTIVO");
            double tarjeta = reporteService.totalPorMetodoPago(desde, hasta, "TARJETA");
            double sinpe = reporteService.totalPorMetodoPago(desde, hasta, "SINPE");
            double totalGastos = reporteService.totalGastos(desde, hasta);
            double totalMerma = reporteService.totalMerma(desde, hasta);
            double ganancia = reporteService.gananciaReal(desde, hasta);

            // === Hoja: Resumen ===
            Sheet resumen = wb.createSheet("Resumen");
            int r = 0;

            Row titulo = resumen.createRow(r++);
            Cell cTitulo = titulo.createCell(0);
            cTitulo.setCellValue("Reporte POS SellControl");
            cTitulo.setCellStyle(boldStyle);

            Row periodo = resumen.createRow(r++);
            periodo.createCell(0).setCellValue("Período:");
            periodo.createCell(1).setCellValue(desde + " a " + hasta);

            r++; // fila vacía

            crearFilaResumen(resumen, r++, "Total Ventas", totalVentas, boldStyle, moneyStyle);
            crearFilaResumen(resumen, r++, "Ventas Cobradas", cobradas, boldStyle, moneyStyle);
            crearFilaResumen(resumen, r++, "Ventas Pendientes", pendientes, boldStyle, moneyStyle);

            r++;
            crearFilaResumen(resumen, r++, "Efectivo", efectivo, boldStyle, moneyStyle);
            crearFilaResumen(resumen, r++, "Tarjeta", tarjeta, boldStyle, moneyStyle);
            crearFilaResumen(resumen, r++, "SINPE", sinpe, boldStyle, moneyStyle);

            r++;
            crearFilaResumen(resumen, r++, "Total Gastos", totalGastos, boldStyle, moneyStyle);
            crearFilaResumen(resumen, r++, "Total Merma", totalMerma, boldStyle, moneyStyle);

            r++;
            crearFilaResumen(resumen, r++, "GANANCIA REAL", ganancia, boldStyle, moneyStyle);

            resumen.autoSizeColumn(0);
            resumen.autoSizeColumn(1);

            // === Hoja: Ventas ===
            Sheet hVentas = wb.createSheet("Ventas");
            String[] colVentas = { "ID", "Fecha/Hora", "Cajero", "Total", "Método Pago", "Estado", "Cliente" };
            crearEncabezado(hVentas, colVentas, headerStyle);

            int rv = 1;
            for (Venta v : ventas) {
                Row row = hVentas.createRow(rv++);
                row.createCell(0).setCellValue(v.getId());
                row.createCell(1).setCellValue(v.getFechaHora());
                row.createCell(2).setCellValue(v.getNombreUsuario());
                Cell cTotal = row.createCell(3);
                cTotal.setCellValue(v.getTotal());
                cTotal.setCellStyle(moneyStyle);
                row.createCell(4).setCellValue(v.getMetodoPago());
                row.createCell(5).setCellValue(v.getEstado());
                row.createCell(6).setCellValue(v.getClienteNombre() != null ? v.getClienteNombre() : "");
            }
            for (int i = 0; i < colVentas.length; i++)
                hVentas.autoSizeColumn(i);

            // === Hoja: Gastos ===
            Sheet hGastos = wb.createSheet("Gastos");
            String[] colGastos = { "ID", "Tipo", "Monto", "Descripción", "Fecha" };
            crearEncabezado(hGastos, colGastos, headerStyle);

            int rg = 1;
            for (Gasto g : gastos) {
                Row row = hGastos.createRow(rg++);
                row.createCell(0).setCellValue(g.getId());
                row.createCell(1).setCellValue(g.getTipo());
                Cell cMonto = row.createCell(2);
                cMonto.setCellValue(g.getMonto());
                cMonto.setCellStyle(moneyStyle);
                row.createCell(3).setCellValue(g.getDescripcion());
                row.createCell(4).setCellValue(g.getFechaHora());
            }
            for (int i = 0; i < colGastos.length; i++)
                hGastos.autoSizeColumn(i);

            // === Hoja: Merma ===
            Sheet hMerma = wb.createSheet("Merma");
            String[] colMerma = { "ID", "Descripción", "Monto Aprox.", "Fecha" };
            crearEncabezado(hMerma, colMerma, headerStyle);

            int rm = 1;
            for (Merma m : mermas) {
                Row row = hMerma.createRow(rm++);
                row.createCell(0).setCellValue(m.getId());
                row.createCell(1).setCellValue(m.getDescripcion());
                Cell cMonto = row.createCell(2);
                cMonto.setCellValue(m.getMontoAproximado());
                cMonto.setCellStyle(moneyStyle);
                row.createCell(3).setCellValue(m.getFechaHora());
            }
            for (int i = 0; i < colMerma.length; i++)
                hMerma.autoSizeColumn(i);

            // Escribir archivo
            try (FileOutputStream fos = new FileOutputStream(destino)) {
                wb.write(fos);
            }

            System.out.println("[ExcelExport] Archivo exportado: " + destino.getAbsolutePath());
            return null;

        } catch (Exception e) {
            System.err.println("[ExcelExport] Error: " + e.getMessage());
            e.printStackTrace();
            return "Error al exportar: " + e.getMessage();
        }
    }

    private void crearEncabezado(Sheet sheet, String[] columnas, CellStyle style) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < columnas.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(style);
        }
    }

    private void crearFilaResumen(Sheet sheet, int rowIdx, String label, double value,
            CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowIdx);
        Cell cLabel = row.createCell(0);
        cLabel.setCellValue(label);
        cLabel.setCellStyle(labelStyle);
        Cell cValue = row.createCell(1);
        cValue.setCellValue(value);
        cValue.setCellStyle(valueStyle);
    }

    private CellStyle crearEstiloEncabezado(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloMoneda(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        DataFormat fmt = wb.createDataFormat();
        style.setDataFormat(fmt.getFormat("₡#,##0.00"));
        return style;
    }
}
