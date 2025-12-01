package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
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
        try {
            System.out.println("=== CARGANDO DASHBOARD ===");

            Map<String, Object> dashboardData = dashboardService.getDashboardData();

            // Pasar los datos a la vista
            model.addAttribute("movimientosRecientes", dashboardData.get("movimientosRecientes"));
            model.addAttribute("productosSinMovimientos", dashboardData.get("productosSinMovimientos"));
            model.addAttribute("productosStockBajo", dashboardData.get("productosStockBajo"));

            // Agregar contadores separados para evitar el error de comparación
            List<?> productosSinMovimientosList = (List<?>) dashboardData.get("productosSinMovimientos");
            List<?> productosStockBajoList = (List<?>) dashboardData.get("productosStockBajo");

            int cantidadSinMovimientos = productosSinMovimientosList != null ? productosSinMovimientosList.size() : 0;
            int cantidadStockBajo = productosStockBajoList != null ? productosStockBajoList.size() : 0;

            model.addAttribute("cantidadSinMovimientos", cantidadSinMovimientos);
            model.addAttribute("cantidadStockBajo", cantidadStockBajo);

            System.out.println("✅ Dashboard cargado:");
            System.out.println("   - Movimientos recientes: " + ((List<?>)dashboardData.get("movimientosRecientes")).size());
            System.out.println("   - Productos sin movimientos: " + cantidadSinMovimientos);
            System.out.println("   - Productos con stock bajo: " + cantidadStockBajo);

            return "dashboard";

        } catch (Exception e) {
            System.err.println("❌ ERROR al cargar dashboard: " + e.getMessage());
            e.printStackTrace();

            // En caso de error, establecer valores por defecto
            model.addAttribute("movimientosRecientes", new ArrayList<>());
            model.addAttribute("productosSinMovimientos", new ArrayList<>());
            model.addAttribute("productosStockBajo", new ArrayList<>());
            model.addAttribute("cantidadSinMovimientos", 0);
            model.addAttribute("cantidadStockBajo", 0);

            return "dashboard";
        }
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