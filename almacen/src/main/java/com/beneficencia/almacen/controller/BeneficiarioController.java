package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.model.Beneficiario;
import com.beneficencia.almacen.service.BeneficiarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/beneficiario")
public class BeneficiarioController {

    @Autowired
    private BeneficiarioService beneficiarioService;

    /**
     * Página principal - LISTA de beneficiarios
     */
    @GetMapping
    public String listarBeneficiarios(
            @RequestParam(value = "busqueda", required = false) String busqueda,
            Model model) {

        System.out.println("GET /beneficiario - Busqueda: " + busqueda);

        List<Beneficiario> beneficiarios;

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            List<Beneficiario> todos = beneficiarioService.obtenerTodosBeneficiarios();
            String busquedaLower = busqueda.toLowerCase();

            beneficiarios = todos.stream()
                    .filter(b ->
                            (b.getDni() != null && b.getDni().contains(busqueda)) ||
                                    (b.getNombres() != null && b.getNombres().toLowerCase().contains(busquedaLower)) ||
                                    (b.getApellidos() != null && b.getApellidos().toLowerCase().contains(busquedaLower))
                    )
                    .toList();
        } else {
            beneficiarios = beneficiarioService.obtenerTodosBeneficiarios();
        }

        System.out.println("Encontrados " + beneficiarios.size() + " beneficiarios");

        model.addAttribute("beneficiarios", beneficiarios);
        model.addAttribute("totalBeneficiarios", beneficiarios.size());
        model.addAttribute("busqueda", busqueda);

        return "beneficiario/lista";
    }

    /**
     * Mostrar formulario para NUEVO beneficiario
     */
    @GetMapping("/formulario")
    public String mostrarFormularioNuevo(Model model) {
        System.out.println("GET /beneficiario/formulario");
        model.addAttribute("beneficiario", new Beneficiario());
        return "beneficiario/formulario";
    }

    /**
     * Guardar NUEVO beneficiario con validación
     */
    @PostMapping("/guardar")
    public String guardarBeneficiario(
            @Valid @ModelAttribute Beneficiario beneficiario,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("POST /beneficiario/guardar - DNI: " + beneficiario.getDni());

            // Validar campos con BindingResult
            if (bindingResult.hasErrors()) {
                System.err.println("Errores de validación encontrados: " + bindingResult.getAllErrors());
                return "beneficiario/formulario";
            }

            // Validar DNI único (además de las validaciones de Bean)
            if (beneficiarioService.existePorDni(beneficiario.getDni())) {
                bindingResult.rejectValue("dni", "error.beneficiario",
                        "Ya existe un beneficiario con el DNI: " + beneficiario.getDni());
                return "beneficiario/formulario";
            }

            // Establecer fechas
            beneficiario.setFechaRegistro(LocalDateTime.now());
            beneficiario.setFechaActualizacion(LocalDateTime.now());

            Beneficiario guardado = beneficiarioService.guardarBeneficiario(beneficiario);
            redirectAttributes.addFlashAttribute("success",
                    "Beneficiario " + guardado.getNombreCompleto() + " registrado exitosamente");

            System.out.println("Beneficiario guardado ID: " + guardado.getId());
            return "redirect:/beneficiario";

        } catch (Exception e) {
            System.err.println("Error al guardar beneficiario: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Error al registrar beneficiario: " + e.getMessage());
            return "redirect:/beneficiario/formulario";
        }
    }

    /**
     * Mostrar formulario para EDITAR beneficiario
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        System.out.println("GET /beneficiario/editar/" + id);

        Optional<Beneficiario> beneficiarioOpt = beneficiarioService.obtenerBeneficiarioPorId(id);

        if (beneficiarioOpt.isPresent()) {
            model.addAttribute("beneficiario", beneficiarioOpt.get());
            return "beneficiario/formulario";
        } else {
            System.err.println("Beneficiario no encontrado ID: " + id);
            return "redirect:/beneficiario?error=Beneficiario+no+encontrado";
        }
    }

    /**
     * Actualizar beneficiario existente con validación
     */
    @PostMapping("/actualizar/{id}")
    public String actualizarBeneficiario(
            @PathVariable Long id,
            @Valid @ModelAttribute Beneficiario beneficiario,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("POST /beneficiario/actualizar/" + id);

            // Validar campos con BindingResult
            if (bindingResult.hasErrors()) {
                System.err.println("Errores de validación encontrados: " + bindingResult.getAllErrors());
                return "beneficiario/formulario";
            }

            // Verificar que exista
            Optional<Beneficiario> existenteOpt = beneficiarioService.obtenerBeneficiarioPorId(id);
            if (existenteOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Beneficiario no encontrado");
                return "redirect:/beneficiario";
            }

            Beneficiario existente = existenteOpt.get();

            // Si cambió el DNI, verificar que no exista otro
            if (!existente.getDni().equals(beneficiario.getDni())) {
                if (beneficiarioService.existePorDni(beneficiario.getDni())) {
                    bindingResult.rejectValue("dni", "error.beneficiario",
                            "Ya existe otro beneficiario con el DNI: " + beneficiario.getDni());
                    return "beneficiario/formulario";
                }
            }

            // Mantener datos existentes que no están en el formulario
            beneficiario.setId(id);
            beneficiario.setFechaRegistro(existente.getFechaRegistro());
            beneficiario.setFechaActualizacion(LocalDateTime.now());

            Beneficiario actualizado = beneficiarioService.guardarBeneficiario(beneficiario);
            redirectAttributes.addFlashAttribute("success",
                    "Beneficiario " + actualizado.getNombreCompleto() + " actualizado exitosamente");

            System.out.println("Beneficiario actualizado ID: " + id);
            return "redirect:/beneficiario";

        } catch (Exception e) {
            System.err.println("Error al actualizar beneficiario: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Error al actualizar beneficiario: " + e.getMessage());
            return "redirect:/beneficiario/editar/" + id;
        }
    }

    /**
     * ELIMINAR beneficiario
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarBeneficiario(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("GET /beneficiario/eliminar/" + id);

            beneficiarioService.eliminarBeneficiario(id);
            redirectAttributes.addFlashAttribute("success",
                    "Beneficiario eliminado exitosamente");

            System.out.println("Beneficiario eliminado ID: " + id);
            return "redirect:/beneficiario";

        } catch (Exception e) {
            System.err.println("Error al eliminar beneficiario: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Error al eliminar beneficiario: " + e.getMessage());
            return "redirect:/beneficiario";
        }
    }
}