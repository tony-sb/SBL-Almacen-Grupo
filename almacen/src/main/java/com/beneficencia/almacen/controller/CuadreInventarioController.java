package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.model.CuadreInventario;
import com.beneficencia.almacen.model.Producto;
import com.beneficencia.almacen.service.CuadreInventarioService;
import com.beneficencia.almacen.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/cuadre-inventario")
public class CuadreInventarioController {

    @Autowired
    private CuadreInventarioService cuadreService;

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public String mostrarCuadreInventario(Model model) {
        // Obtener TODOS los cuadres
        List<CuadreInventario> cuadres = cuadreService.obtenerTodosCuadresOrdenados();

        // Obtener TODOS los productos para el modal
        List<Producto> todosProductos = productoService.obtenerTodosProductos();

        model.addAttribute("cuadres", cuadres); // CAMBIA ESTA LÍNEA
        model.addAttribute("todosProductos", todosProductos);
        model.addAttribute("totalCuadres", cuadres.size());

        return "cuadre-inventario/lista";
    }

    @PostMapping("/guardar")
    public String guardarCuadre(
            @RequestParam Long productoId,
            @RequestParam Integer cantidad,
            @RequestParam(required = false) String fechaVencimiento,
            @RequestParam(required = false) String observaciones,
            @RequestParam String accion,
            RedirectAttributes redirectAttributes) {

        try {
            Producto producto = productoService.obtenerProductoPorId(productoId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

            CuadreInventario cuadre = new CuadreInventario();
            cuadre.setProducto(producto);
            cuadre.setCantidad(cantidad);

            if (fechaVencimiento != null && !fechaVencimiento.isEmpty()) {
                cuadre.setFechaVencimiento(LocalDate.parse(fechaVencimiento));
            }

            cuadre.setObservaciones(observaciones != null ? observaciones : "Sin observaciones");
            cuadre.setAccion(accion);
            cuadre.setEstado("PENDIENTE");

            cuadreService.guardarCuadre(cuadre);

            redirectAttributes.addFlashAttribute("success",
                    "Producto agregado a Stand By: " + producto.getNombre() +
                            " (" + cantidad + " unidades)");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al agregar producto a Stand By: " + e.getMessage());
        }

        return "redirect:/cuadre-inventario";
    }

    @PostMapping("/confirmar/{id}")
    public String confirmarCuadre(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            cuadreService.confirmarCuadre(id);
            redirectAttributes.addFlashAttribute("success", "Producto reingresado al inventario");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al confirmar: " + e.getMessage());
        }

        return "redirect:/cuadre-inventario";
    }

    @PostMapping("/cancelar/{id}")
    public String cancelarCuadre(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            cuadreService.descartarCuadre(id);
            redirectAttributes.addFlashAttribute("success", "Cuadre cancelado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar: " + e.getMessage());
        }

        return "redirect:/cuadre-inventario";
    }
}