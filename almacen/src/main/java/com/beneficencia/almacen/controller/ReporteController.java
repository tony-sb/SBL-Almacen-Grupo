package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Controlador para la generación y descarga de reportes del sistema.
 * Maneja la creación de reportes en formato PDF para diferentes aspectos del inventario.
 */
@Controller
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    /**
     * Genera y descarga un reporte PDF del inventario actual del dashboard.
     * Crea un archivo PDF con un resumen del estado del inventario incluyendo
     * productos, stock bajo y estadísticas relevantes.
     *
     * El nombre del archivo incluye timestamp para evitar sobreescrituras y
     * facilitar el seguimiento de descargas.
     *
     * @return ResponseEntity con el archivo PDF como stream para descarga
     * @throws RuntimeException si ocurre un error durante la generación del PDF
     */
    @GetMapping("/descargar-inventario")
    public ResponseEntity<InputStreamResource> descargarInventarioPDF() {
        try {
            // Usar el método del DASHBOARD para generar el reporte
            ByteArrayInputStream pdf = reporteService.generarReporteDashboard();

            // Generar nombre de archivo con timestamp para unicidad
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "resumen_dashboard_" + timestamp + ".pdf";

            // Configurar headers para forzar descarga del archivo
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + filename);

            // Retornar respuesta con el PDF como recurso descargable
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(pdf));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage());
        }
    }
}