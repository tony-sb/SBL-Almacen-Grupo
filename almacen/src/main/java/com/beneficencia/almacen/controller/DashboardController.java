package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model) {
        // Obtener TODOS los datos desde el servicio que conecta con la BD
        Map<String, Object> dashboardData = dashboardService.getDashboardData();

        // Pasar los datos a la vista HTML
        model.addAttribute("movimientosRecientes", dashboardData.get("movimientosRecientes"));
        model.addAttribute("productosSinMovimientos", dashboardData.get("productosSinMovimientos"));
        model.addAttribute("productosStockBajo", dashboardData.get("productosStockBajo"));

        return "dashboard";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}