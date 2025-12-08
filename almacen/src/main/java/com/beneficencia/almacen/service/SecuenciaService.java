package com.beneficencia.almacen.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class SecuenciaService {

    private static final String PREFIJO_ORDEN_SALIDA = "OS";
    private static final String PREFIJO_TRAMITE = "TRAM";

    public String generarNumeroOrdenSalida() {
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8, 13);
        return PREFIJO_ORDEN_SALIDA + "-" + fecha + "-" + timestamp;
    }

    public String generarNumeroTramite() {
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String secuencia = String.valueOf(System.currentTimeMillis()).substring(10, 13);
        return PREFIJO_TRAMITE + "-" + fecha + "-" + secuencia;
    }
}