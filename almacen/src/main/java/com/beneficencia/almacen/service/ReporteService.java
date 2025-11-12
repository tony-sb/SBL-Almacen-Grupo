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

    // MÉTODO PARA EL INVENTARIO COMPLETO (NUEVO)
    public ByteArrayInputStream generarReporteInventario() {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Título del reporte
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("INVENTARIO DE PRODUCTOS", titleFont);
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

            // Crear tabla con 5 columnas (igual que tu vista web)
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // Configurar anchos de columnas
            float[] columnWidths = {35f, 15f, 15f, 15f, 20f};
            table.setWidths(columnWidths);

            // Encabezados de la tabla (igual que tu vista web)
            String[] headers = {"Nombre Producto", "Código", "Cantidad", "Grupo", "Estado"};
            for (String headerText : headers) {
                PdfPCell header = new PdfPCell();
                header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                header.setBorderWidth(1);
                header.setPhrase(new Phrase(headerText));
                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                header.setPadding(5);
                table.addCell(header);
            }

            // Obtener todos los productos para el inventario
            List<Producto> productos = productoRepository.findAll();

            // Agregar productos al PDF
            for (Producto producto : productos) {
                // Nombre del producto
                PdfPCell cellNombre = new PdfPCell(new Phrase(producto.getNombre()));
                cellNombre.setPadding(5);
                table.addCell(cellNombre);

                // Código
                String codigo = producto.getCodigo() != null ? producto.getCodigo() : "-";
                PdfPCell cellCodigo = new PdfPCell(new Phrase(codigo));
                cellCodigo.setPadding(5);
                cellCodigo.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellCodigo);

                // Cantidad
                String cantidad = producto.getCantidad() != null ? producto.getCantidad().toString() : "-";
                PdfPCell cellCantidad = new PdfPCell(new Phrase(cantidad));
                cellCantidad.setPadding(5);
                cellCantidad.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellCantidad);

                // Grupo (categoría)
                String grupo = producto.getCategoria() != null ? producto.getCategoria() : "-";
                PdfPCell cellGrupo = new PdfPCell(new Phrase(grupo));
                cellGrupo.setPadding(5);
                cellGrupo.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellGrupo);

                // Estado (calculado)
                String estado = calcularEstadoProducto(producto);
                PdfPCell cellEstado = new PdfPCell(new Phrase(estado));
                cellEstado.setPadding(5);
                cellEstado.setHorizontalAlignment(Element.ALIGN_CENTER);

                // Color según el estado
                if ("STOCK BAJO".equals(estado)) {
                    cellEstado.setBackgroundColor(new BaseColor(255, 200, 200)); // Rojo suave
                    cellEstado.setPhrase(new Phrase(estado, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK)));
                } else if ("NORMAL".equals(estado)) {
                    cellEstado.setBackgroundColor(new BaseColor(200, 255, 200)); // Verde suave
                    cellEstado.setPhrase(new Phrase(estado, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK)));
                } else {
                    cellEstado.setBackgroundColor(new BaseColor(220, 220, 220)); // Gris suave
                    cellEstado.setPhrase(new Phrase(estado, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK)));
                }

                table.addCell(cellEstado);
            }

            // Agregar estadísticas al final
            document.add(table);

            // Agregar resumen
            Paragraph resumen = new Paragraph("\n\nRESUMEN DEL INVENTARIO",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            resumen.setSpacingAfter(10);
            document.add(resumen);

            long totalProductos = productos.size();
            long stockBajo = productos.stream()
                    .filter(this::tieneStockBajo)
                    .count();
            long stockNormal = totalProductos - stockBajo;

            Paragraph stats = new Paragraph(
                    String.format("Total de productos: %d | Stock normal: %d | Stock bajo: %d",
                            totalProductos, stockNormal, stockBajo),
                    FontFactory.getFont(FontFactory.HELVETICA, 12)
            );
            document.add(stats);

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage());
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    // MÉTODO PARA EL DASHBOARD (MOVIMIENTOS RECIENTES)
    public ByteArrayInputStream generarReporteDashboard() {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Título del reporte
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("RESUMEN", titleFont);
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

            // Crear tabla con 4 columnas (para el dashboard)
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // Configurar anchos de columnas
            float[] columnWidths = {40f, 20f, 20f, 20f};
            table.setWidths(columnWidths);

            // Encabezados de la tabla (SOLO 4 COLUMNAS para dashboard)
            String[] headers = {"Nombre Producto", "Fecha Salida", "Cantidad", "DNI Beneficiario"};
            for (String headerText : headers) {
                PdfPCell header = new PdfPCell();
                header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                header.setBorderWidth(1);
                header.setPhrase(new Phrase(headerText));
                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                header.setPadding(5);
                table.addCell(header);
            }

            // Obtener datos para el reporte DEL DASHBOARD
            List<MovimientoReciente> movimientos = movimientoRecienteRepository.findAllByOrderByFechaSalidaDesc();
            List<Producto> todosProductos = productoRepository.findAll();

            // Agregar productos con movimientos (SOLO 4 COLUMNAS)
            for (MovimientoReciente movimiento : movimientos) {
                // Nombre del producto
                PdfPCell cellNombre = new PdfPCell(new Phrase(movimiento.getProducto().getNombre()));
                cellNombre.setPadding(5);
                table.addCell(cellNombre);

                // Fecha de salida
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

    private String calcularEstadoProducto(Producto producto) {
        if (producto.getCantidad() == null) {
            return "SIN DATOS";
        }

        Integer stockMinimo = producto.getStockMinimo() != null ? producto.getStockMinimo() : 5;

        if (producto.getCantidad() <= stockMinimo) {
            return "STOCK BAJO";
        } else {
            return "NORMAL";
        }
    }

    private boolean tieneStockBajo(Producto producto) {
        if (producto.getCantidad() == null) return true;
        Integer stockMinimo = producto.getStockMinimo() != null ? producto.getStockMinimo() : 5;
        return producto.getCantidad() <= stockMinimo;
    }
}