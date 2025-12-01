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

    /**
     * Procesa el guardado de una nueva orden de salida.
     * Valida el stock disponible, actualiza el inventario y genera número de orden automático.
     *
     * @param numeroTramite Número de trámite asociado a la orden
     * @param fechaSalida Fecha de salida de los productos
     * @param nombreUsuario Nombre del usuario que recibe los productos
     * @param dniUsuario DNI del usuario que recibe los productos
     * @param descripcion Descripción o motivo de la salida
     * @param productoId ID del producto a retirar
     * @param cantidad Cantidad de productos a retirar
     * @param redirectAttributes Atributos para mensajes flash en redirección
     * @return Redirección a la lista de órdenes de salida
     */
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
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista 'ordenes-salida/imprimir-orden-salida' o vista de error
     */
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

    /**
     * Muestra el formulario para editar una orden de salida existente.
     * Carga los datos de la orden especificada por ID para su modificación.
     *
     * @param id ID de la orden a editar
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
     * @param ordenSalida Objeto de orden con los datos actualizados
     * @param redirectAttributes Atributos para mensajes flash en redirección
     * @return Redirección a la lista de órdenes de salida
     */
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

    /**
     * Genera un número de orden automático basado en el total de órdenes existentes.
     * Formato: XXX-YYYY donde XXX es el número consecutivo e YYYY es el año actual.
     *
     * @return Número de orden generado automáticamente
     */
    private String generarNumeroOrden() {
        Long totalOrdenes = ordenSalidaService.contarTotalOrdenes();
        int siguienteNumero = totalOrdenes.intValue() + 1;
        return String.format("%03d-%d", siguienteNumero, LocalDate.now().getYear());
    }
}