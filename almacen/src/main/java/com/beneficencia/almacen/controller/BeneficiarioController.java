package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.model.Beneficiario;
import com.beneficencia.almacen.model.Producto;
import com.beneficencia.almacen.service.BeneficiarioService;
import com.beneficencia.almacen.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/beneficiario")
public class BeneficiarioController {

    @Autowired
    private BeneficiarioService beneficiarioService;
    @Autowired
    private ProductoService productoService;

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

    @GetMapping("/formulario-con-redireccion")
    public String mostrarFormularioConRedireccion(Model model) {
        System.out.println("GET /beneficiario/formulario-con-redireccion");

        // Crear nuevo beneficiario con datos prefijados si vienen de orden de salida
        Beneficiario beneficiario = new Beneficiario();

        // Obtener datos de flash attributes
        Map<String, ?> flashAttributes = model.asMap();

        if (flashAttributes.containsKey("dni")) {
            beneficiario.setDni((String) flashAttributes.get("dni"));
        }
        if (flashAttributes.containsKey("nombreCompleto")) {
            String nombreCompleto = (String) flashAttributes.get("nombreCompleto");
            beneficiario.setNombres(extraerNombres(nombreCompleto));
            beneficiario.setApellidos(extraerApellidos(nombreCompleto));
        }

        model.addAttribute("beneficiario", beneficiario);
        model.addAttribute("redirigirDesdeOrden", true);

        // Pasar datos de la orden pendiente
        if (flashAttributes.containsKey("productoId")) {
            model.addAttribute("productoId", flashAttributes.get("productoId"));
        }
        if (flashAttributes.containsKey("cantidad")) {
            model.addAttribute("cantidad", flashAttributes.get("cantidad"));
        }
        if (flashAttributes.containsKey("numeroTramite")) {
            model.addAttribute("numeroTramite", flashAttributes.get("numeroTramite"));
        }
        if (flashAttributes.containsKey("fechaSalida")) {
            model.addAttribute("fechaSalida", flashAttributes.get("fechaSalida"));
        }
        if (flashAttributes.containsKey("descripcion")) {
            model.addAttribute("descripcion", flashAttributes.get("descripcion"));
        }

        return "beneficiario/formulario-con-redireccion";
    }
    @PostMapping("/guardar-y-continuar")
    public String guardarBeneficiarioYContinuar(
            @Valid @ModelAttribute Beneficiario beneficiario,
            BindingResult bindingResult,
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) Integer cantidad,
            @RequestParam(required = false) String numeroTramite,
            @RequestParam(required = false) String fechaSalida,
            @RequestParam(required = false) String descripcion,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("POST /beneficiario/guardar-y-continuar");

            // Validar campos
            if (bindingResult.hasErrors()) {
                System.err.println("Errores de validación encontrados: " + bindingResult.getAllErrors());
                return "beneficiario/formulario-con-redireccion";
            }

            // Validar DNI único
            if (beneficiarioService.existePorDni(beneficiario.getDni())) {
                bindingResult.rejectValue("dni", "error.beneficiario",
                        "Ya existe un beneficiario con el DNI: " + beneficiario.getDni());
                return "beneficiario/formulario-con-redireccion";
            }

            // Establecer fechas
            beneficiario.setFechaRegistro(LocalDateTime.now());
            beneficiario.setFechaActualizacion(LocalDateTime.now());

            // Guardar beneficiario
            Beneficiario guardado = beneficiarioService.guardarBeneficiario(beneficiario);

            // Si hay datos de orden pendiente, redirigir a completar la orden
            if (productoId != null && cantidad != null) {
                redirectAttributes.addFlashAttribute("success",
                        "Beneficiario " + guardado.getNombreCompleto() + " registrado exitosamente. Complete la orden de salida.");

                // Pasar datos a la vista de continuar orden
                redirectAttributes.addFlashAttribute("beneficiarioId", guardado.getId());
                redirectAttributes.addFlashAttribute("productoId", productoId);
                redirectAttributes.addFlashAttribute("cantidad", cantidad);
                redirectAttributes.addFlashAttribute("numeroTramite", numeroTramite);
                redirectAttributes.addFlashAttribute("fechaSalida", fechaSalida);
                redirectAttributes.addFlashAttribute("descripcion", descripcion);

                return "redirect:/beneficiario/confirmar-orden";
            }

            redirectAttributes.addFlashAttribute("success",
                    "Beneficiario " + guardado.getNombreCompleto() + " registrado exitosamente");
            return "redirect:/beneficiario";

        } catch (Exception e) {
            System.err.println("Error al guardar beneficiario: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Error al registrar beneficiario: " + e.getMessage());
            return "redirect:/beneficiario/formulario-con-redireccion";
        }
    }

    @GetMapping("/confirmar-orden")
    public String mostrarConfirmacionOrden(Model model) {
        System.out.println("GET /beneficiario/confirmar-orden");

        // Obtener datos de flash attributes
        Map<String, ?> flashAttributes = model.asMap();

        if (!flashAttributes.containsKey("beneficiarioId") ||
                !flashAttributes.containsKey("productoId") ||
                !flashAttributes.containsKey("cantidad")) {
            return "redirect:/ordenes-salida?error=Datos+de+orden+no+disponibles";
        }

        // Obtener datos del beneficiario
        Long beneficiarioId = (Long) flashAttributes.get("beneficiarioId");
        Beneficiario beneficiario = beneficiarioService.obtenerBeneficiarioPorId(beneficiarioId)
                .orElseThrow(() -> new RuntimeException("Beneficiario no encontrado"));

        // Obtener datos del producto
        Long productoId = (Long) flashAttributes.get("productoId");
        Producto producto = productoService.obtenerProductoPorId(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        model.addAttribute("beneficiario", beneficiario);
        model.addAttribute("producto", producto);
        model.addAttribute("cantidad", flashAttributes.get("cantidad"));
        model.addAttribute("numeroTramite", flashAttributes.get("numeroTramite"));
        model.addAttribute("fechaSalida", flashAttributes.get("fechaSalida"));
        model.addAttribute("descripcion", flashAttributes.get("descripcion"));

        return "beneficiario/confirmar-orden";
    }

    // Métodos auxiliares para extraer nombres y apellidos
    private String extraerNombres(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return "";
        }
        String[] partes = nombreCompleto.trim().split(" ");
        if (partes.length >= 2) {
            return partes[0] + (partes.length > 2 ? " " + partes[1] : "");
        }
        return partes[0];
    }

    private String extraerApellidos(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return "";
        }
        String[] partes = nombreCompleto.trim().split(" ");
        if (partes.length >= 3) {
            StringBuilder apellidos = new StringBuilder();
            for (int i = 2; i < partes.length; i++) {
                apellidos.append(partes[i]);
                if (i < partes.length - 1) {
                    apellidos.append(" ");
                }
            }
            return apellidos.toString();
        } else if (partes.length == 2) {
            return partes[1];
        }
        return "";
    }
}