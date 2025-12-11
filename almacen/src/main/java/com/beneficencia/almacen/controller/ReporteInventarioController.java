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

@Controller
public class ReporteInventarioController {

    @Autowired
    private ReporteService reporteService;

    @GetMapping("/descargar-inventario-completo")
    public ResponseEntity<InputStreamResource> descargarInventarioPDF() {
        try {
            ByteArrayInputStream pdf = reporteService.generarReporteInventario();

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "inventario_completo_" + timestamp + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + filename);

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