package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.model.Beneficiario;
import com.beneficencia.almacen.model.OrdenSalida;
import com.beneficencia.almacen.model.OrdenSalidaItem;
import com.beneficencia.almacen.model.Producto;
import com.beneficencia.almacen.repository.OrdenSalidaRepository;
import com.beneficencia.almacen.repository.UsuarioRepository;
import com.beneficencia.almacen.service.BeneficiarioService;
import com.beneficencia.almacen.service.OrdenSalidaService;
import com.beneficencia.almacen.service.ProductoService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

/**
 * Controlador para la gestión de órdenes de salida del almacén.
 * Maneja las operaciones de creación, consulta, edición, eliminación e impresión
 * de órdenes de salida de productos del inventario.
 */
@Controller
@RequestMapping("/ordenes-salida")
public class OrdenSalidaController {

    @Autowired
    private OrdenSalidaService ordenSalidaService;

    @Autowired
    private OrdenSalidaRepository ordenSalidaRepository;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private BeneficiarioService beneficiarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Muestra la página principal de órdenes de salida con opciones de filtrado.
     * Permite filtrar órdenes por período (año/mes) o realizar búsquedas por DNI o número de trámite.
     *
     * @param periodo Año para filtrar las órdenes (por defecto 2025)
     * @param mes Mes para filtrar las órdenes (0 para todo el año)
     * @param busqueda Término de búsqueda por DNI o número de trámite
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista 'ordenes-salida'
     */
    // Array de meses en español (para usar en la vista)
    private final String[] MESES_ESPANOL = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    @GetMapping
    public String mostrarPaginaOrdenSalida(
            @RequestParam(value = "busqueda", required = false) String busqueda,
            Model model) {

        LocalDate hoy = LocalDate.now();
        int añoActual = hoy.getYear();
        int mesActual = hoy.getMonthValue();
        String nombreMes = MESES_ESPANOL[mesActual - 1];
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

        List<OrdenSalida> ordenesSalida;

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            if (busqueda.matches("\\d{8}")) {
                ordenesSalida = ordenSalidaService.buscarPorDniUsuario(busqueda);
            } else {
                ordenesSalida = ordenSalidaService.buscarPorNumeroTramite(busqueda);
            }

            // Ordenar los resultados de búsqueda también
            if (ordenesSalida != null && !ordenesSalida.isEmpty()) {
                ordenesSalida.sort((o1, o2) -> o2.getFechaSalida().compareTo(o1.getFechaSalida()));
            }

        } else {
            ordenesSalida = ordenSalidaService.obtenerTodasOrdenes();
        }

        model.addAttribute("ordenesSalida", ordenesSalida);
        model.addAttribute("añoActual", añoActual);
        model.addAttribute("mesActual", mesActual);
        model.addAttribute("nombreMes", nombreMes);
        model.addAttribute("hoy", hoy);
        model.addAttribute("busqueda", busqueda);

        return "ordenes-salida";
    }

    /**
     * Procesa el guardado de una nueva orden de salida.
     * Valida el stock disponible, actualiza el inventario y genera número de orden automático.
     *
     * @param numeroTramite      Número de trámite asociado a la orden
     * @param fechaSalida        Fecha de salida de los productos
     * @param nombreUsuario      Nombre del usuario que recibe los productos
     * @param dniUsuario         DNI del usuario que recibe los productos
     * @param descripcion        Descripción o motivo de la salida
     * @param productoId         ID del producto a retirar
     * @param cantidad           Cantidad de productos a retirar
     * @param redirectAttributes Atributos para mensajes flash en redirección
     * @return Redirección a la lista de órdenes de salida
     */
    @PostMapping("/guardar")
    public String guardarOrdenSalida(
            @RequestParam(required = false) String numeroTramite,
            @RequestParam String fechaSalida,
            @RequestParam String nombreUsuario,
            @RequestParam String dniUsuario,
            @RequestParam String descripcion,
            @RequestParam Long productoId,
            @RequestParam Integer cantidad,
            RedirectAttributes redirectAttributes) {

        try {
            // Validar DNI (8 dígitos)
            if (dniUsuario == null || !dniUsuario.matches("\\d{8}")) {
                throw new RuntimeException("El DNI debe tener exactamente 8 dígitos");
            }

            // 1. Verificar si el beneficiario existe
            boolean beneficiarioExiste = beneficiarioService.existePorDni(dniUsuario);

            if (!beneficiarioExiste) {
                // Redirigir al formulario de registro de beneficiario
                redirectAttributes.addFlashAttribute("dni", dniUsuario);
                redirectAttributes.addFlashAttribute("nombreCompleto", nombreUsuario);
                redirectAttributes.addFlashAttribute("redirigirDesdeOrden", true);
                redirectAttributes.addFlashAttribute("productoId", productoId);
                redirectAttributes.addFlashAttribute("cantidad", cantidad);
                redirectAttributes.addFlashAttribute("numeroTramite", numeroTramite);
                redirectAttributes.addFlashAttribute("fechaSalida", fechaSalida);
                redirectAttributes.addFlashAttribute("descripcion", descripcion);

                return "redirect:/beneficiario/formulario-con-redireccion";
            }

            // 2. Obtener el beneficiario
            Beneficiario beneficiario = beneficiarioService.obtenerBeneficiarioPorDni(dniUsuario)
                    .orElseThrow(() -> new RuntimeException("Beneficiario no encontrado"));

            // 3. Crear la orden de salida
            OrdenSalida ordenSalida = new OrdenSalida();
            ordenSalida.setNumeroTramite(numeroTramite);
            ordenSalida.setFechaSalida(LocalDate.parse(fechaSalida));
            ordenSalida.setNombreUsuario(nombreUsuario);
            ordenSalida.setDniUsuario(dniUsuario);
            ordenSalida.setDescripcion(descripcion);
            ordenSalida.setBeneficiario(beneficiario);

            // 4. Obtener el producto
            Producto producto = productoService.obtenerProductoPorId(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (producto.getCantidad() < cantidad) {
                throw new RuntimeException("Stock insuficiente. Stock disponible: " + producto.getCantidad());
            }

            // 5. Crear el item de la orden
            OrdenSalidaItem item = new OrdenSalidaItem();
            item.setProducto(producto);
            item.setCantidad(cantidad);
            item.setPrecioUnitario(producto.getPrecioUnitario());

            // 6. Guardar la orden con el item
            ordenSalida.agregarItem(item);
            ordenSalidaService.guardarOrdenConItems(ordenSalida, ordenSalida.getItems());

            redirectAttributes.addFlashAttribute("success", "Orden de salida guardada exitosamente");
            redirectAttributes.addFlashAttribute("numeroOrden", ordenSalida.getNumeroOrden());
            return "redirect:/ordenes-salida?success";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar la orden: " + e.getMessage());
            return "redirect:/ordenes-salida?error";
        }
    }
    // Métodos auxiliares para separar nombres y apellidos
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
    // Nuevo método para continuar la orden después de registrar beneficiario
    @PostMapping("/continuar-con-beneficiario")
    public String continuarOrdenConBeneficiario(
            @RequestParam Long beneficiarioId,
            @RequestParam Long productoId,
            @RequestParam Integer cantidad,
            @RequestParam(required = false) String numeroTramite,
            @RequestParam String fechaSalida,
            @RequestParam String descripcion,
            RedirectAttributes redirectAttributes) {

        try {
            // Obtener beneficiario
            Beneficiario beneficiario = beneficiarioService.obtenerBeneficiarioPorId(beneficiarioId)
                    .orElseThrow(() -> new RuntimeException("Beneficiario no encontrado"));

            // Crear orden de salida
            OrdenSalida ordenSalida = new OrdenSalida();
            ordenSalida.setNumeroTramite(numeroTramite);
            ordenSalida.setFechaSalida(LocalDate.parse(fechaSalida));
            ordenSalida.setNombreUsuario(beneficiario.getNombreCompleto());
            ordenSalida.setDniUsuario(beneficiario.getDni());
            ordenSalida.setDescripcion(descripcion);
            ordenSalida.setBeneficiario(beneficiario);

            // Obtener producto
            Producto producto = productoService.obtenerProductoPorId(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (producto.getCantidad() < cantidad) {
                throw new RuntimeException("Stock insuficiente. Stock disponible: " + producto.getCantidad());
            }

            // Crear item
            OrdenSalidaItem item = new OrdenSalidaItem();
            item.setProducto(producto);
            item.setCantidad(cantidad);
            item.setPrecioUnitario(producto.getPrecioUnitario());

            // Guardar orden
            ordenSalida.agregarItem(item);
            ordenSalidaService.guardarOrdenConItems(ordenSalida, ordenSalida.getItems());

            redirectAttributes.addFlashAttribute("success", "Orden de salida guardada exitosamente");
            redirectAttributes.addFlashAttribute("numeroOrden", ordenSalida.getNumeroOrden());
            return "redirect:/ordenes-salida?success";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al completar la orden: " + e.getMessage());
            return "redirect:/beneficiario?error";
        }
    }

    // Nuevo endpoint para verificar DNI
    @GetMapping("/verificar-dni/{dni}")
    @ResponseBody
    public Map<String, Object> verificarDni(@PathVariable String dni) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validar formato DNI
            if (dni == null || !dni.matches("\\d{8}")) {
                response.put("valido", false);
                response.put("mensaje", "DNI debe tener 8 dígitos");
                return response;
            }

            Optional<Beneficiario> beneficiarioOpt = beneficiarioService.obtenerBeneficiarioPorDni(dni);

            if (beneficiarioOpt.isPresent()) {
                Beneficiario beneficiario = beneficiarioOpt.get();
                response.put("existe", true);
                response.put("nombreCompleto", beneficiario.getNombreCompleto());
                response.put("nombres", beneficiario.getNombres());
                response.put("apellidos", beneficiario.getApellidos());
                response.put("telefono", beneficiario.getTelefono());
                response.put("direccion", beneficiario.getDireccion());
                response.put("mensaje", "Beneficiario encontrado");
            } else {
                response.put("existe", false);
                response.put("mensaje", "DNI no registrado. Complete los datos manualmente.");
            }
            response.put("valido", true);

        } catch (Exception e) {
            response.put("valido", false);
            response.put("mensaje", "Error al verificar DNI: " + e.getMessage());
        }

        return response;
    }

    /**
     * Endpoint REST para obtener la lista de todos los productos disponibles.
     * Utilizado para cargar dinámicamente los productos en los formularios.
     *
     * @return Lista de todos los productos en formato JSON
     */
    @GetMapping("/productos")
    @ResponseBody
    public List<Producto> obtenerProductos() {
        return productoService.obtenerTodosProductos();
    }

    /**
     * Elimina una orden de salida mediante una petición AJAX.
     * Retorna una respuesta JSON indicando el resultado de la operación.
     *
     * @param id ID de la orden a eliminar
     * @return Map con el resultado de la operación (success y message)
     */
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

    /**
     * Muestra la vista optimizada para imprimir una orden de salida.
     * Busca la orden por su número de orden y carga todos los datos para la impresión.
     *
     * @param numeroOrden Número de orden a imprimir
     * @param model       Modelo para pasar datos a la vista
     * @return Nombre de la vista 'ordenes-salida/imprimir-orden-salida' o vista de error
     */
    @GetMapping("/imprimir/{numeroOrden}")
    public String imprimirOrden(@PathVariable String numeroOrden, Model model) {
        try {
            Optional<OrdenSalida> ordenSalidaOpt = ordenSalidaRepository.findByNumeroOrden(numeroOrden);
            if (!ordenSalidaOpt.isPresent()) {
                throw new RuntimeException("Orden no encontrada: " + numeroOrden);
            }
            OrdenSalida ordenSalida = ordenSalidaOpt.get();
            model.addAttribute("ordenSalida", ordenSalida);
            return "ordenes-salida/imprimir-orden-salida";
        } catch (Exception e) {
            model.addAttribute("error", "Error al generar impresión: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Muestra el formulario para editar una orden de salida existente.
     * Carga los datos de la orden especificada por ID para su modificación.
     *
     * @param id    ID de la orden a editar
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista 'ordenes-salida/editar-orden-salida'
     */
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

    /**
     * Procesa la actualización de una orden de salida existente.
     * Actualiza los campos modificados de la orden sin afectar el stock.
     *
     * @param ordenSalida        Objeto de orden con los datos actualizados
     * @param redirectAttributes Atributos para mensajes flash en redirección
     * @return Redirección a la lista de órdenes de salida
     */
    @PostMapping("/actualizar")
    @Transactional
    public String actualizarOrdenSalida(
            @ModelAttribute OrdenSalida ordenSalida,
            RedirectAttributes redirectAttributes) {
        try {
            OrdenSalida ordenExistente = ordenSalidaService.obtenerOrdenPorId(ordenSalida.getId())
                    .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + ordenSalida.getId()));

            // Actualizar datos básicos
            ordenExistente.setNumeroTramite(ordenSalida.getNumeroTramite());
            ordenExistente.setFechaSalida(ordenSalida.getFechaSalida());
            ordenExistente.setNombreUsuario(ordenSalida.getNombreUsuario());
            ordenExistente.setDniUsuario(ordenSalida.getDniUsuario());
            ordenExistente.setDescripcion(ordenSalida.getDescripcion());
            ordenExistente.setFechaActualizacion(LocalDateTime.now());

            // Verificar si cambió el DNI y actualizar beneficiario
            if (!ordenExistente.getDniUsuario().equals(ordenSalida.getDniUsuario())) {
                Beneficiario beneficiario = beneficiarioService.obtenerBeneficiarioPorDni(ordenSalida.getDniUsuario())
                        .orElse(null);
                if (beneficiario == null) {
                    // Crear nuevo beneficiario
                    beneficiario = new Beneficiario();
                    beneficiario.setDni(ordenSalida.getDniUsuario());
                    beneficiario.setNombres(extraerNombres(ordenSalida.getNombreUsuario()));
                    beneficiario.setApellidos(extraerApellidos(ordenSalida.getNombreUsuario()));
                    beneficiario.setFechaRegistro(LocalDateTime.now());
                    beneficiario.setFechaActualizacion(LocalDateTime.now());
                    beneficiario = beneficiarioService.guardarBeneficiario(beneficiario);
                }
                ordenExistente.setBeneficiario(beneficiario);
            }

            ordenSalidaService.guardarOrden(ordenExistente);
            redirectAttributes.addFlashAttribute("success", "Orden actualizada exitosamente");
            return "redirect:/ordenes-salida?success";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la orden: " + e.getMessage());
            return "redirect:/ordenes-salida?error";
        }
    }
    /**
     * Guarda una orden de salida con múltiples productos.
     */
    @PostMapping("/guardar-multiples")
    public String guardarOrdenSalidaMultiples(
            @RequestParam(required = false) String numeroTramite,
            @RequestParam String fechaSalida,
            @RequestParam String nombreUsuario,
            @RequestParam String dniUsuario,
            @RequestParam String descripcion,
            @RequestParam List<Long> productoIds,
            @RequestParam List<Integer> cantidades,
            RedirectAttributes redirectAttributes) {

        try {
            // Validar DNI
            if (dniUsuario == null || !dniUsuario.matches("\\d{8}")) {
                throw new RuntimeException("El DNI debe tener exactamente 8 dígitos");
            }

            // Validar que haya productos
            if (productoIds == null || productoIds.isEmpty()) {
                throw new RuntimeException("Debe seleccionar al menos un producto");
            }

            // Validar que las listas tengan el mismo tamaño
            if (productoIds.size() != cantidades.size()) {
                throw new RuntimeException("Error en los datos de productos");
            }

            // Verificar o crear beneficiario
            boolean beneficiarioExiste = beneficiarioService.existePorDni(dniUsuario);

            if (!beneficiarioExiste) {
                // Redirigir al formulario de beneficiario
                redirectAttributes.addFlashAttribute("dni", dniUsuario);
                redirectAttributes.addFlashAttribute("nombreCompleto", nombreUsuario);
                redirectAttributes.addFlashAttribute("redirigirDesdeOrden", true);
                redirectAttributes.addFlashAttribute("numeroTramite", numeroTramite);
                redirectAttributes.addFlashAttribute("fechaSalida", fechaSalida);
                redirectAttributes.addFlashAttribute("descripcion", descripcion);

                // Guardar productos temporalmente (puedes usar sesión o localStorage en el frontend)
                redirectAttributes.addFlashAttribute("productoIds", productoIds);
                redirectAttributes.addFlashAttribute("cantidades", cantidades);

                return "redirect:/beneficiario/formulario-con-redireccion";
            }

            // Obtener beneficiario
            Beneficiario beneficiario = beneficiarioService.obtenerBeneficiarioPorDni(dniUsuario)
                    .orElseThrow(() -> new RuntimeException("Beneficiario no encontrado"));

            // Crear la orden de salida
            OrdenSalida ordenSalida = new OrdenSalida();
            ordenSalida.setNumeroTramite(numeroTramite);
            ordenSalida.setFechaSalida(LocalDate.parse(fechaSalida));
            ordenSalida.setNombreUsuario(nombreUsuario);
            ordenSalida.setDniUsuario(dniUsuario);
            ordenSalida.setDescripcion(descripcion);
            ordenSalida.setBeneficiario(beneficiario);

            // Crear items para cada producto
            List<OrdenSalidaItem> items = new ArrayList<>();

            for (int i = 0; i < productoIds.size(); i++) {
                Long productoId = productoIds.get(i);
                Integer cantidad = cantidades.get(i);

                // Obtener producto
                Producto producto = productoService.obtenerProductoPorId(productoId)
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));

                // Validar stock
                if (producto.getCantidad() < cantidad) {
                    throw new RuntimeException("Stock insuficiente para " + producto.getNombre() +
                            ". Disponible: " + producto.getCantidad() + ", Solicitado: " + cantidad);
                }

                // Crear item
                OrdenSalidaItem item = new OrdenSalidaItem();
                item.setProducto(producto);
                item.setCantidad(cantidad);
                item.setPrecioUnitario(producto.getPrecioUnitario());

                items.add(item);
            }

            // Guardar la orden con todos los items
            ordenSalidaService.guardarOrdenConItems(ordenSalida, items);

            redirectAttributes.addFlashAttribute("success", "Orden de salida guardada exitosamente");
            redirectAttributes.addFlashAttribute("numeroOrden", ordenSalida.getNumeroOrden());
            return "redirect:/ordenes-salida?success";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar la orden: " + e.getMessage());
            return "redirect:/ordenes-salida?error";
        }
    }
}