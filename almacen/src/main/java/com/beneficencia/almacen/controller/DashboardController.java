package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model) {
        try {

            Map<String, Object> dashboardData = dashboardService.getDashboardData();

            model.addAttribute("movimientosRecientes", dashboardData.get("movimientosRecientes"));
            model.addAttribute("productosSinMovimientos", dashboardData.get("productosSinMovimientos"));
            model.addAttribute("productosStockBajo", dashboardData.get("productosStockBajo"));

            List<?> productosSinMovimientosList = (List<?>) dashboardData.get("productosSinMovimientos");
            List<?> productosStockBajoList = (List<?>) dashboardData.get("productosStockBajo");

            int cantidadSinMovimientos = productosSinMovimientosList != null ? productosSinMovimientosList.size() : 0;
            int cantidadStockBajo = productosStockBajoList != null ? productosStockBajoList.size() : 0;

            model.addAttribute("cantidadSinMovimientos", cantidadSinMovimientos);
            model.addAttribute("cantidadStockBajo", cantidadStockBajo);

            return "dashboard";

        } catch (Exception e) {
            System.err.println("ERROR al cargar dashboard: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("movimientosRecientes", new ArrayList<>());
            model.addAttribute("productosSinMovimientos", new ArrayList<>());
            model.addAttribute("productosStockBajo", new ArrayList<>());
            model.addAttribute("cantidadSinMovimientos", 0);
            model.addAttribute("cantidadStockBajo", 0);

            return "dashboard";
        }
    }

    @GetMapping("/error/access-denied")
    public String accesoDenegado(@RequestParam(value = "reason", required = false) String reason,
                                 Model model) {

        if ("disabled".equals(reason)) {
            model.addAttribute("titulo", "Cuenta Deshabilitada");
            model.addAttribute("mensaje", "Tu cuenta está deshabilitada. Contacta al administrador del sistema.");
        } else {
            model.addAttribute("titulo", "Acceso Denegado");
            model.addAttribute("mensaje", "No tienes los permisos necesarios para acceder a esta sección.");
        }
        return "error/access-denied";
    }
}