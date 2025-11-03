package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.MovimientoReciente;
import com.beneficencia.almacen.model.Producto;
import com.beneficencia.almacen.repository.MovimientoRecienteRepository;
import com.beneficencia.almacen.repository.ProductoRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
public class ReporteService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoRecienteRepository movimientoRecienteRepository;

    public ByteArrayInputStream generarReporteInventario() {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Título del reporte
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("REPORTE DE INVENTARIO", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Información de la institución
            Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Paragraph institution = new Paragraph("Sociedad de Beneficencia de Lambayeque", infoFont);
            institution.setAlignment(Element.ALIGN_CENTER);
            document.add(institution);

            Paragraph date = new Paragraph("Fecha de reporte: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), infoFont);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(20);
            document.add(date);

            // Crear tabla con 4 columnas (eliminando Stock Actual)
            PdfPTable table = new PdfPTable(4); // CAMBIADO de 5 a 4 columnas
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // Configurar anchos de columnas (sin la columna de Stock)
            float[] columnWidths = {40f, 20f, 20f, 20f}; // CAMBIADO: solo 4 valores
            table.setWidths(columnWidths);

            // Encabezados de la tabla (SOLO 4 COLUMNAS)
            String[] headers = {"Nombre Producto", "Fecha Salida", "Cantidad", "DNI Beneficiario"}; // ELIMINADO "Stock Actual"
            for (String headerText : headers) {
                PdfPCell header = new PdfPCell();
                header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                header.setBorderWidth(1);
                header.setPhrase(new Phrase(headerText));
                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                header.setPadding(5);
                table.addCell(header);
            }

            // Obtener datos para el reporte
            List<MovimientoReciente> movimientos = movimientoRecienteRepository.findAllByOrderByFechaSalidaDesc();
            List<Producto> todosProductos = productoRepository.findAll();

            // Agregar productos con movimientos (SOLO 4 COLUMNAS)
            for (MovimientoReciente movimiento : movimientos) {
                // Nombre del producto
                PdfPCell cellNombre = new PdfPCell(new Phrase(movimiento.getProducto().getNombre()));
                cellNombre.setPadding(5);
                table.addCell(cellNombre);

                // Fecha de salida - MANEJO SEGURO PARA LocalDate
                String fechaStr = "-";
                if (movimiento.getFechaSalida() != null) {
                    try {
                        fechaStr = movimiento.getFechaSalida().format(
                                DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        );
                    } catch (Exception e) {
                        fechaStr = movimiento.getFechaSalida().toString();
                    }
                }
                PdfPCell cellFecha = new PdfPCell(new Phrase(fechaStr));
                cellFecha.setPadding(5);
                cellFecha.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellFecha);

                // Cantidad
                PdfPCell cellCantidad = new PdfPCell(new Phrase(String.valueOf(movimiento.getCantidad())));
                cellCantidad.setPadding(5);
                cellCantidad.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellCantidad);

                // DNI Beneficiario
                String dni = movimiento.getDniBeneficiario() != null ? movimiento.getDniBeneficiario() : "-";
                PdfPCell cellDni = new PdfPCell(new Phrase(dni));
                cellDni.setPadding(5);
                cellDni.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellDni);

                // SE ELIMINÓ LA COLUMNA DE STOCK ACTUAL
            }

            // Agregar productos sin movimientos (SOLO 4 COLUMNAS)
            for (Producto producto : todosProductos) {
                boolean tieneMovimientos = movimientos.stream()
                        .anyMatch(m -> m.getProducto().getId().equals(producto.getId()));

                if (!tieneMovimientos) {
                    // Nombre del producto
                    PdfPCell cellNombre = new PdfPCell(new Phrase(producto.getNombre()));
                    cellNombre.setPadding(5);
                    table.addCell(cellNombre);

                    // Fecha de salida (vacío)
                    PdfPCell cellFecha = new PdfPCell(new Phrase("-"));
                    cellFecha.setPadding(5);
                    cellFecha.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cellFecha);

                    // Cantidad (vacío)
                    PdfPCell cellCantidad = new PdfPCell(new Phrase("-"));
                    cellCantidad.setPadding(5);
                    cellCantidad.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cellCantidad);

                    // DNI Beneficiario (vacío)
                    PdfPCell cellDni = new PdfPCell(new Phrase("-"));
                    cellDni.setPadding(5);
                    cellDni.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cellDni);

                    // SE ELIMINÓ LA COLUMNA DE STOCK ACTUAL
                }
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage());
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}