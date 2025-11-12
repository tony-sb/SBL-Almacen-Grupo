package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.model.Proveedor;
import com.beneficencia.almacen.service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    /**
     * Mostrar lista de todos los proveedores
     */
    @GetMapping
    public String listarProveedores(Model model) {
        model.addAttribute("proveedores", proveedorService.obtenerTodosProveedores());
        return "proveedores/lista";
    }

    /**
     * Mostrar formulario para nuevo proveedor
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoProveedor(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "proveedores/formulario";
    }

    /**
     * Procesar guardado de nuevo proveedor
     */
    @PostMapping("/guardar")
    public String guardarProveedor(@ModelAttribute Proveedor proveedor,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Verificar si ya existe un proveedor con el mismo RUC
            if (proveedorService.existeProveedorConRuc(proveedor.getRuc())) {
                redirectAttributes.addFlashAttribute("error",
                        "Ya existe un proveedor con el RUC: " + proveedor.getRuc());
                return "redirect:/proveedores/nuevo";
            }

            proveedorService.guardarProveedor(proveedor);
            redirectAttributes.addFlashAttribute("success",
                    "Proveedor guardado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al guardar el proveedor: " + e.getMessage());
        }
        return "redirect:/proveedores";
    }

    /**
     * Mostrar formulario para editar proveedor
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarProveedor(@PathVariable Long id,
                                                   Model model,
                                                   RedirectAttributes redirectAttributes) {
        Optional<Proveedor> proveedor = proveedorService.obtenerProveedorPorId(id);
        if (proveedor.isPresent()) {
            model.addAttribute("proveedor", proveedor.get());
            return "proveedores/formulario";
        } else {
            redirectAttributes.addFlashAttribute("error", "Proveedor no encontrado");
            return "redirect:/proveedores";
        }
    }

    /**
     * Procesar actualización de proveedor
     */
    @PostMapping("/actualizar")
    public String actualizarProveedor(@ModelAttribute Proveedor proveedor,
                                      RedirectAttributes redirectAttributes) {
        try {
            // Verificar si el RUC ya existe en otro proveedor
            Optional<Proveedor> proveedorExistente = proveedorService.obtenerProveedorPorRuc(proveedor.getRuc());
            if (proveedorExistente.isPresent() &&
                    !proveedorExistente.get().getId().equals(proveedor.getId())) {
                redirectAttributes.addFlashAttribute("error",
                        "Ya existe otro proveedor con el RUC: " + proveedor.getRuc());
                return "redirect:/proveedores/editar/" + proveedor.getId();
            }

            proveedorService.guardarProveedor(proveedor);
            redirectAttributes.addFlashAttribute("success",
                    "Proveedor actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al actualizar el proveedor: " + e.getMessage());
        }
        return "redirect:/proveedores";
    }

    /**
     * Eliminar proveedor
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarProveedor(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            proveedorService.eliminarProveedor(id);
            redirectAttributes.addFlashAttribute("success",
                    "Proveedor eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al eliminar el proveedor: " + e.getMessage());
        }
        return "redirect:/proveedores";
    }

    /**
     * Buscar proveedores por nombre
     */
    @GetMapping("/buscar")
    public String buscarProveedores(@RequestParam String q, Model model) {
        model.addAttribute("proveedores", proveedorService.buscarProveedoresPorNombre(q));
        model.addAttribute("terminoBusqueda", q);
        return "proveedores/lista";
    }
}