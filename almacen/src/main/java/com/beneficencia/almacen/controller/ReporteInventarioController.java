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
 * Controlador especializado para la generación y descarga de reportes de inventario.
 * Maneja la creación de reportes detallados del inventario completo en formato PDF.
 */
@Controller
public class ReporteInventarioController {

    @Autowired
    private ReporteService reporteService;

    /**
     * Genera y descarga un reporte PDF del inventario completo del sistema.
     * Crea un archivo PDF con información detallada de todos los productos
     * incluyendo stock, categorías, precios y alertas de inventario.
     *
     * El reporte incluye timestamp en el nombre del archivo para garantizar
     * unicidad y facilitar el control de versiones de los reportes descargados.
     *
     * @return ResponseEntity con el archivo PDF como stream para descarga
     * @throws RuntimeException si ocurre un error durante la generación del PDF
     */
    @GetMapping("/descargar-inventario-completo")
    public ResponseEntity<InputStreamResource> descargarInventarioPDF() {
        try {
            // Usar el método del INVENTARIO COMPLETO para generar el reporte detallado
            ByteArrayInputStream pdf = reporteService.generarReporteInventario();

            // Generar nombre de archivo con timestamp para identificar la descarga
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "inventario_completo_" + timestamp + ".pdf";

            // Configurar headers HTTP para forzar la descarga del archivo
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