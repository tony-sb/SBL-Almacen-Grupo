package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.model.Proveedor;
import com.beneficencia.almacen.service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controlador para la gestión de proveedores del almacén.
 * Maneja las operaciones CRUD (Crear, Leer, Actualizar, Eliminar) de proveedores
 * incluyendo validaciones de RUC único y búsqueda por nombre.
 */
@Controller
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    /**
     * Muestra la lista de todos los proveedores registrados en el sistema.
     * Recupera todos los proveedores existentes y los pasa a la vista para su visualización.
     *
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista 'proveedores/lista'
     */
    @GetMapping
    public String listarProveedores(Model model) {
        model.addAttribute("proveedores", proveedorService.obtenerTodosProveedores());
        return "proveedores/lista";
    }

    /**
     * Muestra el formulario para crear un nuevo proveedor.
     * Prepara el modelo con un proveedor vacío para ser completado en el formulario.
     *
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista 'proveedores/formulario'
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoProveedor(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "proveedores/formulario";
    }

    /**
     * Procesa el guardado de un nuevo proveedor.
     * Valida la unicidad del RUC antes de proceder con el guardado.
     * Maneja excepciones y proporciona retroalimentación al usuario.
     *
     * @param proveedor Objeto Proveedor con los datos del formulario
     * @param redirectAttributes Atributos para mensajes flash en redirección
     * @return Redirección a la lista de proveedores o al formulario en caso de error
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
     * Muestra el formulario para editar un proveedor existente.
     * Busca el proveedor por ID y carga sus datos en el formulario para edición.
     * Si el proveedor no existe, redirige con un mensaje de error.
     *
     * @param id ID del proveedor a editar
     * @param model Modelo para pasar datos a la vista
     * @param redirectAttributes Atributos para mensajes flash en redirección
     * @return Nombre de la vista 'proveedores/formulario' o redirección en caso de error
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
     * Procesa la actualización de un proveedor existente.
     * Valida que el RUC no esté siendo utilizado por otro proveedor antes de actualizar.
     * Maneja excepciones y proporciona retroalimentación al usuario.
     *
     * @param proveedor Objeto Proveedor con los datos actualizados
     * @param redirectAttributes Atributos para mensajes flash en redirección
     * @return Redirección a la lista de proveedores o al formulario en caso de error
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
     * Elimina un proveedor del sistema.
     * Busca el proveedor por ID y procede con su eliminación si existe.
     * Maneja excepciones y proporciona retroalimentación al usuario.
     *
     * @param id ID del proveedor a eliminar
     * @param redirectAttributes Atributos para mensajes flash en redirección
     * @return Redirección a la lista de proveedores
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
     * Busca proveedores por nombre utilizando un término de búsqueda.
     * Realiza una búsqueda que incluye el término proporcionado en el nombre del proveedor.
     *
     * @param q Término de búsqueda para filtrar proveedores por nombre
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista 'proveedores/lista' con los resultados filtrados
     */
    @GetMapping("/buscar")
    public String buscarProveedores(@RequestParam String q, Model model) {
        model.addAttribute("proveedores", proveedorService.buscarProveedoresPorNombre(q));
        model.addAttribute("terminoBusqueda", q);
        return "proveedores/lista";
    }
}