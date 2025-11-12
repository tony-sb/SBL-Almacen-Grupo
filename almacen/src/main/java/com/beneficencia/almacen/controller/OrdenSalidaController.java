package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.model.OrdenSalida;
import com.beneficencia.almacen.model.Producto;
import com.beneficencia.almacen.repository.OrdenSalidaRepository;
import com.beneficencia.almacen.service.OrdenSalidaService;
import com.beneficencia.almacen.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/ordenes-salida")
public class OrdenSalidaController {

    @Autowired
    private OrdenSalidaService ordenSalidaService;

    @Autowired
    private OrdenSalidaRepository ordenSalidaRepository;

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public String mostrarPaginaOrdenSalida(
            @RequestParam(value = "periodo", required = false, defaultValue = "2025") Integer periodo,
            @RequestParam(value = "mes", required = false, defaultValue = "0") Integer mes,
            @RequestParam(value = "busqueda", required = false) String busqueda,
            Model model) {

        List<OrdenSalida> ordenesSalida;

        // Primero verificar si hay búsqueda por DNI o trámite
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            // Buscar tanto por DNI como por número de trámite
            List<OrdenSalida> porDni = ordenSalidaService.buscarPorDniUsuario(busqueda);
            List<OrdenSalida> porTramite = ordenSalidaService.buscarPorNumeroTramite(busqueda);

            // Combinar resultados (evitar duplicados)
            ordenesSalida = porDni;
            for (OrdenSalida orden : porTramite) {
                if (!ordenesSalida.contains(orden)) {
                    ordenesSalida.add(orden);
                }
            }
        } else {
            // Filtrar por período y mes
            if (mes != null && mes > 0) {
                YearMonth yearMonth = YearMonth.of(periodo, mes);
                LocalDate fechaInicio = yearMonth.atDay(1);
                LocalDate fechaFin = yearMonth.atEndOfMonth();
                ordenesSalida = ordenSalidaService.buscarPorFecha(fechaInicio, fechaFin);
            } else {
                // Todas las órdenes del año
                LocalDate fechaInicio = LocalDate.of(periodo, 1, 1);
                LocalDate fechaFin = LocalDate.of(periodo, 12, 31);
                ordenesSalida = ordenSalidaService.buscarPorFecha(fechaInicio, fechaFin);
            }
        }

        model.addAttribute("ordenesSalida", ordenesSalida);
        model.addAttribute("periodo", periodo);
        model.addAttribute("mes", mes);
        model.addAttribute("busqueda", busqueda);

        return "ordenes-salida";
    }

    @PostMapping("/guardar")
    public String guardarOrdenSalida(
            @RequestParam String numeroTramite,
            @RequestParam String fechaSalida,
            @RequestParam String nombreUsuario,
            @RequestParam String dniUsuario,
            @RequestParam String descripcion,
            @RequestParam Long productoId,
            @RequestParam Integer cantidad,
            RedirectAttributes redirectAttributes) {

        try {
            // Crear y guardar la orden de salida
            OrdenSalida ordenSalida = new OrdenSalida();
            ordenSalida.setNumeroTramite(numeroTramite);
            ordenSalida.setFechaSalida(LocalDate.parse(fechaSalida));
            ordenSalida.setNombreUsuario(nombreUsuario);
            ordenSalida.setDniUsuario(dniUsuario);
            ordenSalida.setDescripcion(descripcion);

            // Generar número de orden automático
            String numeroOrden = generarNumeroOrden();
            ordenSalida.setNumeroOrden(numeroOrden);
            ordenSalida.setCantidadProductos(cantidad);

            // Actualizar stock del producto
            Producto producto = productoService.obtenerProductoPorId(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (producto.getCantidad() < cantidad) {
                throw new RuntimeException("Stock insuficiente. Stock disponible: " + producto.getCantidad());
            }

            producto.setCantidad(producto.getCantidad() - cantidad);
            productoService.actualizarProducto(producto);

            ordenSalidaService.guardarOrden(ordenSalida);

            redirectAttributes.addFlashAttribute("success", "Orden de salida guardada exitosamente");
            return "redirect:/ordenes-salida?success";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar la orden: " + e.getMessage());
            return "redirect:/ordenes-salida?error";
        }
    }

    @GetMapping("/productos")
    @ResponseBody
    public List<Producto> obtenerProductos() {
        return productoService.obtenerTodosProductos();
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public Map<String, Object> eliminarOrden(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            ordenSalidaService.eliminarOrden(id);
            response.put("success", true);
            response.put("message", "Orden eliminada exitosamente");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar la orden: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/imprimir/{numeroOrden}")
    public String imprimirOrden(@PathVariable String numeroOrden, Model model) {
        try {
            System.out.println("Buscando orden para imprimir: " + numeroOrden);

            // Buscar la orden por número de orden usando el repository
            Optional<OrdenSalida> ordenSalidaOpt = ordenSalidaRepository.findByNumeroOrden(numeroOrden);

            if (!ordenSalidaOpt.isPresent()) {
                throw new RuntimeException("Orden no encontrada: " + numeroOrden);
            }

            OrdenSalida ordenSalida = ordenSalidaOpt.get();
            System.out.println("Orden encontrada: " + ordenSalida.getNumeroOrden());
            model.addAttribute("ordenSalida", ordenSalida);

            return "ordenes-salida/imprimir-orden-salida";

        } catch (Exception e) {
            System.err.println("Error al generar impresión: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al generar impresión: " + e.getMessage());
            return "error";
        }
    }

    // SOLO UN MÉTODO PARA EDITAR - ELIMINÉ EL DUPLICADO
    @GetMapping("/editar/{id}")
    public String editarOrden(@PathVariable Long id, Model model) {
        try {
            Optional<OrdenSalida> ordenOpt = ordenSalidaService.obtenerOrdenPorId(id);
            if (!ordenOpt.isPresent()) {
                throw new RuntimeException("Orden no encontrada con ID: " + id);
            }

            OrdenSalida ordenSalida = ordenOpt.get();
            model.addAttribute("ordenSalida", ordenSalida);

            return "ordenes-salida/editar-orden-salida";

        } catch (Exception e) {
            throw new RuntimeException("Error al cargar orden para editar: " + e.getMessage());
        }
    }

    @PostMapping("/actualizar")
    public String actualizarOrdenSalida(
            @ModelAttribute OrdenSalida ordenSalida,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("Actualizando orden ID: " + ordenSalida.getId());

            // Verificar que la orden existe
            OrdenSalida ordenExistente = ordenSalidaService.obtenerOrdenPorId(ordenSalida.getId())
                    .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + ordenSalida.getId()));

            // Actualizar los campos necesarios
            ordenExistente.setNumeroTramite(ordenSalida.getNumeroTramite());
            ordenExistente.setFechaSalida(ordenSalida.getFechaSalida());
            ordenExistente.setNombreUsuario(ordenSalida.getNombreUsuario());
            ordenExistente.setDniUsuario(ordenSalida.getDniUsuario());
            ordenExistente.setDescripcion(ordenSalida.getDescripcion());
            ordenExistente.setCantidadProductos(ordenSalida.getCantidadProductos());

            // Guardar la orden actualizada
            ordenSalidaService.guardarOrden(ordenExistente);

            redirectAttributes.addFlashAttribute("success", "Orden actualizada exitosamente");
            return "redirect:/ordenes-salida?success";

        } catch (Exception e) {
            System.err.println("Error al actualizar orden: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la orden: " + e.getMessage());
            return "redirect:/ordenes-salida?error";
        }
    }

    // Método para generar número de orden automático
    private String generarNumeroOrden() {
        Long totalOrdenes = ordenSalidaService.contarTotalOrdenes();
        int siguienteNumero = totalOrdenes.intValue() + 1;
        return String.format("%03d-%d", siguienteNumero, LocalDate.now().getYear());
    }
}