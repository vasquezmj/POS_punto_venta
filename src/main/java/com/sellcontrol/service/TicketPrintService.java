package com.sellcontrol.service;

import com.sellcontrol.model.DetalleVenta;
import com.sellcontrol.model.Venta;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Servicio de impresión de tickets de venta.
 * Configurado para la impresora térmica HOP-H58 (58mm, USB, ESC/POS).
 * Ancho útil: ~32 caracteres por línea.
 * Busca automáticamente la impresora "HOP-H58" instalada en Windows.
 */
public class TicketPrintService {

    private static final String PRINTER_NAME = "HOP-H58";
    private static final int LINE_WIDTH = 32; // caracteres útiles en 58mm
    private static final Charset CHARSET = Charset.forName("CP437");

    // Comandos ESC/POS
    private static final byte[] ESC_INIT = { 0x1B, 0x40 }; // Inicializar impresora
    private static final byte[] ESC_CENTER = { 0x1B, 0x61, 0x01 }; // Alinear al centro
    private static final byte[] ESC_LEFT = { 0x1B, 0x61, 0x00 }; // Alinear a la izquierda
    private static final byte[] ESC_BOLD_ON = { 0x1B, 0x45, 0x01 }; // Negrita ON
    private static final byte[] ESC_BOLD_OFF = { 0x1B, 0x45, 0x00 }; // Negrita OFF
    private static final byte[] ESC_DOUBLE_HEIGHT = { 0x1B, 0x21, 0x10 }; // Doble altura
    private static final byte[] ESC_NORMAL_SIZE = { 0x1B, 0x21, 0x00 }; // Tamaño normal
    private static final byte[] ESC_CUT = { 0x1D, 0x56, 0x00 }; // Corte de papel
    private static final byte[] ESC_FEED_3 = { 0x1B, 0x64, 0x03 }; // Avanzar 3 líneas
    private static final byte LF = 0x0A; // Salto de línea

    /**
     * Imprime un ticket para la venta indicada.
     *
     * @param venta    la venta a imprimir
     * @param detalles los detalles (líneas) de la venta
     * @param cajero   nombre del cajero
     * @return null si OK, o mensaje de error
     */
    public String imprimir(Venta venta, List<DetalleVenta> detalles, String cajero) {
        try {
            // Buscar impresora por nombre
            PrintService printService = buscarImpresora();
            if (printService == null) {
                return "Impresora '" + PRINTER_NAME + "' no encontrada. Verifique que esté conectada e instalada.";
            }

            // Generar bytes ESC/POS
            byte[] ticketData = generarTicketESCPOS(venta, detalles, cajero);

            // Enviar a la impresora
            DocPrintJob job = printService.createPrintJob();
            Doc doc = new SimpleDoc(ticketData, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
            job.print(doc, new HashPrintRequestAttributeSet());

            System.out.println("[TicketPrint] Ticket impreso para venta #" + venta.getId() + " en " + PRINTER_NAME);
            return null;

        } catch (PrintException e) {
            System.err.println("[TicketPrint] Error de impresión: " + e.getMessage());
            return "Error al imprimir: " + e.getMessage();
        } catch (IOException e) {
            System.err.println("[TicketPrint] Error generando ticket: " + e.getMessage());
            return "Error generando ticket: " + e.getMessage();
        }
    }

    /**
     * Busca la impresora HOP-H58 entre los servicios de impresión del sistema.
     */
    private PrintService buscarImpresora() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService ps : services) {
            if (ps.getName().toLowerCase().contains(PRINTER_NAME.toLowerCase())) {
                System.out.println("[TicketPrint] Impresora encontrada: " + ps.getName());
                return ps;
            }
        }
        // Log todas las impresoras disponibles para debug
        System.err.println("[TicketPrint] Impresora '" + PRINTER_NAME + "' no encontrada. Disponibles:");
        for (PrintService ps : services) {
            System.err.println("  - " + ps.getName());
        }
        return null;
    }

    /**
     * Genera el ticket completo como array de bytes ESC/POS.
     */
    private byte[] generarTicketESCPOS(Venta venta, List<DetalleVenta> detalles, String cajero) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Inicializar
        out.write(ESC_INIT);

        // === Encabezado centrado ===
        out.write(ESC_CENTER);
        out.write(ESC_DOUBLE_HEIGHT);
        out.write(ESC_BOLD_ON);
        escribirLinea(out, "VERDULERIA");
        out.write(ESC_NORMAL_SIZE);
        out.write(ESC_BOLD_OFF);
        escribirLinea(out, "SellControl POS");
        escribirLinea(out, repetir('=', LINE_WIDTH));
        out.write(ESC_LEFT);

        // === Datos de la venta ===
        escribirLinea(out, "Venta #: " + venta.getId());
        escribirLinea(out, "Fecha  : " + venta.getFechaHora());
        escribirLinea(out, "Cajero : " + truncar(cajero, 22));
        escribirLinea(out, "Pago   : " + venta.getMetodoPago());

        if ("PENDIENTE".equals(venta.getEstado())) {
            out.write(ESC_BOLD_ON);
            escribirLinea(out, "Estado : FIADO (PENDIENTE)");
            out.write(ESC_BOLD_OFF);
            if (venta.getClienteNombre() != null && !venta.getClienteNombre().isBlank()) {
                escribirLinea(out, "Cliente: " + truncar(venta.getClienteNombre(), 22));
            }
        }

        // === Líneas de detalle ===
        escribirLinea(out, repetir('-', LINE_WIDTH));
        out.write(ESC_BOLD_ON);
        // Header: Producto Cant Subt
        escribirLinea(out, formatearLineaDetalle("Producto", "Cant", "Subt."));
        out.write(ESC_BOLD_OFF);
        escribirLinea(out, repetir('-', LINE_WIDTH));

        for (DetalleVenta dv : detalles) {
            String nombre = dv.getNombreProducto();
            if (nombre == null)
                nombre = "Prod #" + dv.getProductoId();
            nombre = truncar(nombre, 16);

            String cant = String.format("%.1f", dv.getCantidad());
            if (dv.getTipoUnidad() != null) {
                cant += "KG".equals(dv.getTipoUnidad()) ? "kg" : "u";
            }

            String sub = String.format("%.0f", dv.getSubtotal());

            escribirLinea(out, formatearLineaDetalle(nombre, cant, sub));
        }

        // === Total ===
        escribirLinea(out, repetir('=', LINE_WIDTH));
        out.write(ESC_BOLD_ON);
        out.write(ESC_DOUBLE_HEIGHT);
        String totalStr = String.format("TOTAL: %c%.0f", (char) 0xA2, venta.getTotal()); // ¢ en CP437
        escribirLinea(out, totalStr);
        out.write(ESC_NORMAL_SIZE);
        out.write(ESC_BOLD_OFF);
        escribirLinea(out, repetir('=', LINE_WIDTH));

        // === Pie ===
        out.write(ESC_CENTER);
        escribirLinea(out, "");
        escribirLinea(out, "Gracias por su compra!");
        escribirLinea(out, "");

        // Avanzar y cortar
        out.write(ESC_FEED_3);
        out.write(ESC_CUT);

        return out.toByteArray();
    }

    /**
     * Formatea una línea de detalle con columnas alineadas.
     * Formato: [Producto(16)] [Cant(6)] [Subt(8)]
     */
    private String formatearLineaDetalle(String producto, String cant, String subtotal) {
        return String.format("%-16s %6s %8s", producto, cant, subtotal);
    }

    private void escribirLinea(ByteArrayOutputStream out, String text) throws IOException {
        out.write(text.getBytes(CHARSET));
        out.write(LF);
    }

    private String truncar(String text, int max) {
        if (text == null)
            return "";
        if (text.length() <= max)
            return text;
        return text.substring(0, max - 1) + ".";
    }

    private String repetir(char c, int n) {
        return String.valueOf(c).repeat(n);
    }
}
