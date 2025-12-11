package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.service.EstadisticaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/estadisticas")
public class EstadisticaController {

    @Autowired
    private EstadisticaService estadisticaService;


    @GetMapping("")
    public String mostrarEstadisticas(Model model) {
        try {
            System.out.println("=== CARGANDO PÁGINA DE ESTADÍSTICAS ===");

            List<Map<String, Object>> productosMasSolicitados = estadisticaService.obtenerProductosMasSolicitados();
            List<Map<String, Object>> beneficiariosMasActivos = estadisticaService.obtenerBeneficiariosMasActivos();
            List<Map<String, Object>> entregasPorMes = estadisticaService.obtenerEntregasPorMes();

            model.addAttribute("productosMasSolicitados", productosMasSolicitados);
            model.addAttribute("beneficiariosMasActivos", beneficiariosMasActivos);
            model.addAttribute("entregasPorMes", entregasPorMes);
            model.addAttribute("totalBeneficiarios", estadisticaService.contarTotalBeneficiarios());
            model.addAttribute("totalProductosEntregados", estadisticaService.contarTotalProductosEntregados());
            model.addAttribute("mesConMasEntregas", estadisticaService.obtenerMesConMasEntregas());

            return "estadisticas/graficos";

        } catch (Exception e) {
            System.err.println("ERROR al cargar estadísticas: " + e.getMessage());
            e.printStackTrace();


            model.addAttribute("error", "Error al cargar estadísticas: " + e.getMessage());
            model.addAttribute("productosMasSolicitados", List.of());
            model.addAttribute("beneficiariosMasActivos", List.of());
            model.addAttribute("entregasPorMes", List.of());
            model.addAttribute("totalBeneficiarios", 0);
            model.addAttribute("totalProductosEntregados", 0);
            model.addAttribute("mesConMasEntregas", "Sin datos");

            return "estadisticas/graficos";
        }
    }


    @GetMapping("/api/productos-mas-solicitados")
    @ResponseBody
    public Map<String, Object> obtenerProductosMasSolicitadosJson() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("data", estadisticaService.obtenerProductosMasSolicitados());
            response.put("message", "Productos más solicitados obtenidos");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }


    @GetMapping("/api/beneficiarios-mas-activos")
    @ResponseBody
    public Map<String, Object> obtenerBeneficiariosMasActivosJson() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("data", estadisticaService.obtenerBeneficiariosMasActivos());
            response.put("message", "Beneficiarios más activos obtenidos");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }


    @GetMapping("/api/entregas-por-mes")
    @ResponseBody
    public Map<String, Object> obtenerEntregasPorMesJson() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("data", estadisticaService.obtenerEntregasPorMes());
            response.put("message", "Entregas por mes obtenidas");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
}