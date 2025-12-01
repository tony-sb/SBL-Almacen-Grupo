package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * Controlador para gestionar el dashboard y las páginas principales del sistema.
 * Maneja las vistas del panel de control, acceso denegado y redirección principal.
 */
@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Muestra el dashboard principal con los datos resumidos del sistema.
     * Obtiene los datos del dashboard desde el servicio y los pasa a la vista.
     * Incluye movimientos recientes, productos sin movimientos y productos con stock bajo.
     *
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista 'dashboard' que será renderizada
     */
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

    /**
     * Muestra la página de acceso denegado cuando un usuario intenta acceder
     * a recursos para los que no tiene permisos suficientes.
     *
     * @return Nombre de la vista 'error/access-denied' que será renderizada
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }

    /**
     * Maneja la ruta raíz del sistema y redirige al dashboard principal.
     * Procesa las solicitudes GET a la ruta "/" y redirige automáticamente
     * al dashboard del usuario autenticado.
     *
     * @return Redirección a la ruta "/dashboard"
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}