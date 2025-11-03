package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.model.*;
import com.beneficencia.almacen.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/ordenes-abastecimiento")
public class OrdenAbastecimientoController {

    @Autowired
    private OrdenAbastecimientoService ordenAbastecimientoService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Lista todas las órdenes de abastecimiento (página principal)
     */
    @GetMapping
    public String listarOrdenesAbastecimiento(Model model, Authentication authentication) {
        try {
            System.out.println("=== CARGANDO PÁGINA PRINCIPAL DE ÓRDENES DE ABASTECIMIENTO ===");

            // Cargar datos necesarios
            List<OrdenAbastecimiento> ordenes = ordenAbastecimientoService.obtenerTodasOrdenes();
            List<Proveedor> proveedores = proveedorService.obtenerTodosProveedores();
            List<Producto> productos = productoService.obtenerTodosProductos();

            System.out.println("Órdenes encontradas: " + ordenes.size());
            System.out.println("Proveedores encontrados: " + proveedores.size());
            System.out.println("Productos encontrados: " + productos.size());

            // Agregar atributos al modelo
            model.addAttribute("ordenes", ordenes);
            model.addAttribute("proveedores", proveedores);
            model.addAttribute("productos", productos);
            model.addAttribute("tiposOrden", OrdenAbastecimiento.TipoOrden.values());
            model.addAttribute("ordenAbastecimiento", new OrdenAbastecimiento());

            return "ordenes-abastecimiento";

        } catch (Exception e) {
            System.err.println("ERROR al cargar página principal: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "Error al cargar los datos: " + e.getMessage());
            model.addAttribute("ordenes", new ArrayList<OrdenAbastecimiento>());
            model.addAttribute("proveedores", new ArrayList<Proveedor>());
            model.addAttribute("productos", new ArrayList<Producto>());
            model.addAttribute("tiposOrden", OrdenAbastecimiento.TipoOrden.values());

            return "ordenes-abastecimiento";
        }
    }

    /**
     * Muestra formulario para nueva orden (página completa)
     */
    @GetMapping("/nueva")
    public String mostrarFormularioNuevaOrden(Model model) {
        try {
            System.out.println("=== CARGANDO FORMULARIO NUEVA ORDEN DE ABASTECIMIENTO ===");

            // Cargar datos necesarios
            List<Proveedor> proveedores = proveedorService.obtenerTodosProveedores();
            List<Producto> productos = productoService.obtenerTodosProductos();

            System.out.println("Proveedores cargados: " + proveedores.size());
            System.out.println("Productos cargados: " + productos.size());

            // Log de productos para debugging
            for (Producto producto : productos) {
                System.out.println("Producto: " + producto.getNombre() +
                        " - ID: " + producto.getId() +
                        " - Precio: " + producto.getPrecioUnitario());
            }

            model.addAttribute("ordenAbastecimiento", new OrdenAbastecimiento());
            model.addAttribute("proveedores", proveedores);
            model.addAttribute("productos", productos);
            model.addAttribute("tiposOrden", OrdenAbastecimiento.TipoOrden.values());
            model.addAttribute("modo", "nueva");

            System.out.println("Formulario nueva orden cargado");
            return "form-orden-abastecimiento";

        } catch (Exception e) {
            System.err.println("ERROR al cargar formulario nueva orden: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "redirect:/ordenes-abastecimiento";
        }
    }

    /**
     * Guarda una nueva orden de abastecimiento
     */
    @PostMapping("/guardar")
    public String guardarOrdenAbastecimiento(@ModelAttribute OrdenAbastecimiento ordenAbastecimiento,
                                             @RequestParam Long proveedorId,
                                             @RequestParam(required = false) List<Long> productoIds,
                                             @RequestParam(required = false) List<Integer> cantidades,
                                             @RequestParam(required = false) List<BigDecimal> precios,
                                             Authentication authentication,
                                             Model model,
                                             RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== INICIANDO GUARDADO DE NUEVA ORDEN DE ABASTECIMIENTO ===");
            System.out.println("Tipo orden: " + ordenAbastecimiento.getTipoOrden());
            System.out.println("Proveedor ID: " + proveedorId);

            // Validaciones básicas
            if (proveedorId == null) {
                throw new IllegalArgumentException("Debe seleccionar un proveedor");
            }

            // Obtener el usuario autenticado
            String username = authentication.getName();
            Usuario usuario = usuarioService.obtenerUsuarioPorUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
            System.out.println("Usuario autenticado: " + usuario.getNombre());

            // Establecer proveedor
            Proveedor proveedor = proveedorService.obtenerProveedorPorId(proveedorId)
                    .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + proveedorId));
            ordenAbastecimiento.setProveedor(proveedor);
            System.out.println("Proveedor asignado: " + proveedor.getNombre());

            // Establecer usuario
            ordenAbastecimiento.setUsuario(usuario);

            // Procesar items de la orden
            procesarItemsOrden(ordenAbastecimiento, productoIds, cantidades, precios);

            // Guardar la orden
            OrdenAbastecimiento ordenGuardada = ordenAbastecimientoService.guardarOrden(ordenAbastecimiento);
            System.out.println("Orden guardada exitosamente: " + ordenGuardada.getNumeroOA());

            // Mensaje de éxito
            redirectAttributes.addFlashAttribute("success",
                    "Orden de abastecimiento " + ordenGuardada.getNumeroOA() + " creada exitosamente");

            return "redirect:/ordenes-abastecimiento";

        } catch (Exception e) {
            System.err.println("ERROR al guardar orden: " + e.getMessage());
            e.printStackTrace();

            return recargarFormularioConError(model, ordenAbastecimiento,
                    "Error al guardar la orden: " + e.getMessage());
        }
    }

    /**
     * Muestra formulario para editar orden existente
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarOrden(@PathVariable Long id, Model model) {
        try {
            System.out.println("=== CARGANDO ORDEN PARA EDITAR - ID: " + id + " ===");

            OrdenAbastecimiento ordenAbastecimiento = ordenAbastecimientoService.obtenerOrdenPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada con ID: " + id));

            // Forzar carga de items (evitar LazyInitializationException)
            if (ordenAbastecimiento.getItems() != null) {
                System.out.println("Items encontrados: " + ordenAbastecimiento.getItems().size());
                ordenAbastecimiento.getItems().size(); // Force initialization

                // Log de items para debugging
                for (OrdenAbastecimientoItem item : ordenAbastecimiento.getItems()) {
                    System.out.println("   - " + item.getProducto().getNombre() +
                            " x " + item.getCantidad() +
                            " = S/ " + item.getSubtotal());
                }
            }

            // Cargar datos necesarios
            List<Proveedor> proveedores = proveedorService.obtenerTodosProveedores();
            List<Producto> productos = productoService.obtenerTodosProductos();

            model.addAttribute("ordenAbastecimiento", ordenAbastecimiento);
            model.addAttribute("proveedores", proveedores);
            model.addAttribute("productos", productos);
            model.addAttribute("tiposOrden", OrdenAbastecimiento.TipoOrden.values());
            model.addAttribute("modo", "editar");

            System.out.println("Formulario edición cargado para orden: " + ordenAbastecimiento.getNumeroOA());
            return "form-orden-abastecimiento";

        } catch (Exception e) {
            System.err.println("ERROR al cargar orden para editar: " + e.getMessage());
            e.printStackTrace();

            return "redirect:/ordenes-abastecimiento?error=Orden no encontrada";
        }
    }

    /**
     * Actualiza una orden de abastecimiento existente
     */
    @PostMapping("/editar/{id}")
    public String actualizarOrdenAbastecimiento(@PathVariable Long id,
                                                @ModelAttribute OrdenAbastecimiento ordenAbastecimiento,
                                                @RequestParam Long proveedorId,
                                                @RequestParam(required = false) List<Long> productoIds,
                                                @RequestParam(required = false) List<Integer> cantidades,
                                                @RequestParam(required = false) List<BigDecimal> precios,
                                                Authentication authentication,
                                                Model model,
                                                RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== ACTUALIZANDO ORDEN EXISTENTE - ID: " + id + " ===");

            // Verificar que la orden existe
            OrdenAbastecimiento ordenExistente = ordenAbastecimientoService.obtenerOrdenPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada con ID: " + id));

            // Validaciones
            if (proveedorId == null) {
                throw new IllegalArgumentException("Debe seleccionar un proveedor");
            }

            // Obtener el usuario autenticado
            String username = authentication.getName();
            Usuario usuario = usuarioService.obtenerUsuarioPorUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

            // Establecer proveedor
            Proveedor proveedor = proveedorService.obtenerProveedorPorId(proveedorId)
                    .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + proveedorId));

            // Actualizar datos básicos
            ordenExistente.setTipoOrden(ordenAbastecimiento.getTipoOrden());
            ordenExistente.setFechaOA(ordenAbastecimiento.getFechaOA());
            ordenExistente.setProveedor(proveedor);
            ordenExistente.setObservaciones(ordenAbastecimiento.getObservaciones());
            ordenExistente.setFechaActualizacion(LocalDateTime.now());

            System.out.println("Datos básicos actualizados");

            // Procesar items (limpiar y agregar nuevos)
            procesarItemsParaEdicion(ordenExistente, productoIds, cantidades, precios);

            // Guardar la orden actualizada
            OrdenAbastecimiento ordenActualizada = ordenAbastecimientoService.guardarOrden(ordenExistente);
            System.out.println("Orden actualizada exitosamente: " + ordenActualizada.getNumeroOA());

            // Mensaje de éxito
            redirectAttributes.addFlashAttribute("success",
                    "Orden de abastecimiento " + ordenActualizada.getNumeroOA() + " actualizada exitosamente");

            return "redirect:/ordenes-abastecimiento";

        } catch (Exception e) {
            System.err.println("ERROR al actualizar orden: " + e.getMessage());
            e.printStackTrace();

            return recargarFormularioConError(model, ordenAbastecimiento,
                    "Error al actualizar la orden: " + e.getMessage());
        }
    }

    /**
     * Elimina una orden de abastecimiento
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarOrdenAbastecimiento(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== ELIMINANDO ORDEN - ID: " + id + " ===");

            // Verificar que la orden existe antes de eliminar
            OrdenAbastecimiento orden = ordenAbastecimientoService.obtenerOrdenPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada con ID: " + id));

            String numeroOA = orden.getNumeroOA();
            ordenAbastecimientoService.eliminarOrden(id);

            System.out.println("Orden eliminada: " + numeroOA);

            // Mensaje de éxito
            redirectAttributes.addFlashAttribute("success",
                    "Orden de abastecimiento " + numeroOA + " eliminada exitosamente");

        } catch (Exception e) {
            System.err.println("ERROR al eliminar orden: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("error",
                    "Error al eliminar la orden: " + e.getMessage());
        }

        return "redirect:/ordenes-abastecimiento";
    }

    /**
     * Muestra vista para imprimir orden
     */
    @GetMapping("/imprimir/{id}")
    public String imprimirOrdenAbastecimiento(@PathVariable Long id, Model model) {
        try {
            System.out.println("=== CARGANDO ORDEN PARA IMPRIMIR - ID: " + id + " ===");

            OrdenAbastecimiento ordenAbastecimiento = ordenAbastecimientoService.obtenerOrdenPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada con ID: " + id));

            // Forzar la carga de los items (LAZY loading)
            if (ordenAbastecimiento.getItems() != null) {
                ordenAbastecimiento.getItems().size(); // Force initialization
                System.out.println("Items cargados para impresión: " + ordenAbastecimiento.getItems().size());
            }

            model.addAttribute("ordenAbastecimiento", ordenAbastecimiento);
            System.out.println("Vista de impresión cargada para: " + ordenAbastecimiento.getNumeroOA());

            return "imprimir-orden-abastecimiento";

        } catch (Exception e) {
            System.err.println("ERROR al cargar orden para imprimir: " + e.getMessage());
            e.printStackTrace();

            return "redirect:/ordenes-abastecimiento?error=Error al cargar orden para imprimir";
        }
    }

    // ================= MÉTODOS PRIVADOS AUXILIARES =================

    /**
     * Procesa los items para una nueva orden
     */
    private void procesarItemsOrden(OrdenAbastecimiento ordenAbastecimiento,
                                    List<Long> productoIds,
                                    List<Integer> cantidades,
                                    List<BigDecimal> precios) {

        if (productoIds == null || cantidades == null || precios == null ||
                productoIds.isEmpty() || productoIds.stream().allMatch(Objects::isNull)) {

            System.out.println("No hay items para procesar");
            ordenAbastecimiento.setItems(new ArrayList<>());
            return;
        }

        List<OrdenAbastecimientoItem> items = new ArrayList<>();
        int itemsValidos = 0;

        for (int i = 0; i < productoIds.size(); i++) {
            if (productoIds.get(i) != null && cantidades.get(i) != null && precios.get(i) != null) {

                try {
                    int finalI = i;
                    Producto producto = productoService.obtenerProductoPorId(productoIds.get(i))
                            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado ID: " + productoIds.get(finalI)));

                    OrdenAbastecimientoItem item = new OrdenAbastecimientoItem();
                    item.setProducto(producto);
                    item.setCantidad(cantidades.get(i));
                    item.setPrecioUnitario(precios.get(i));
                    item.setSubtotal(precios.get(i).multiply(BigDecimal.valueOf(cantidades.get(i))));
                    item.setOrdenAbastecimiento(ordenAbastecimiento);
                    items.add(item);

                    itemsValidos++;
                    System.out.println("Item agregado: " + producto.getNombre() +
                            " x " + cantidades.get(i) + " = S/ " + item.getSubtotal());

                } catch (Exception e) {
                    System.err.println("Error procesando item " + i + ": " + e.getMessage());
                }
            }
        }

        if (itemsValidos == 0) {
            throw new IllegalArgumentException("Debe agregar al menos un producto válido a la orden");
        }

        ordenAbastecimiento.setItems(items);
        System.out.println("Total items procesados: " + itemsValidos);
    }

    /**
     * Procesa items para edición (limpia existentes y agrega nuevos)
     */
    private void procesarItemsParaEdicion(OrdenAbastecimiento ordenExistente,
                                          List<Long> productoIds,
                                          List<Integer> cantidades,
                                          List<BigDecimal> precios) {

        // Limpiar items existentes
        ordenExistente.getItems().clear();
        System.out.println("Items existentes eliminados");

        // Procesar nuevos items
        if (productoIds != null && cantidades != null && precios != null &&
                productoIds.size() == cantidades.size() && productoIds.size() == precios.size()) {

            List<OrdenAbastecimientoItem> items = new ArrayList<>();
            int itemsValidos = 0;

            for (int i = 0; i < productoIds.size(); i++) {
                if (productoIds.get(i) != null && cantidades.get(i) != null && precios.get(i) != null) {

                    try {
                        int finalI = i;
                        Producto producto = productoService.obtenerProductoPorId(productoIds.get(i))
                                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado ID: " + productoIds.get(finalI)));

                        OrdenAbastecimientoItem item = new OrdenAbastecimientoItem();
                        item.setProducto(producto);
                        item.setCantidad(cantidades.get(i));
                        item.setPrecioUnitario(precios.get(i));
                        item.setSubtotal(precios.get(i).multiply(BigDecimal.valueOf(cantidades.get(i))));
                        item.setOrdenAbastecimiento(ordenExistente);
                        items.add(item);

                        itemsValidos++;
                        System.out.println("Item actualizado: " + producto.getNombre() +
                                " x " + cantidades.get(i) + " = S/ " + item.getSubtotal());

                    } catch (Exception e) {
                        System.err.println("Error procesando item " + i + " para edición: " + e.getMessage());
                    }
                }
            }

            if (itemsValidos == 0) {
                throw new IllegalArgumentException("Debe agregar al menos un producto válido a la orden");
            }

            ordenExistente.getItems().addAll(items);
            System.out.println("Total items actualizados: " + itemsValidos);
        } else {
            System.out.println("No hay items válidos para actualizar");
            throw new IllegalArgumentException("Los datos de los productos no son válidos");
        }
    }

    /**
     * Recarga el formulario con datos y mensaje de error
     */
    private String recargarFormularioConError(Model model, OrdenAbastecimiento ordenAbastecimiento, String mensajeError) {
        try {
            List<Proveedor> proveedores = proveedorService.obtenerTodosProveedores();
            List<Producto> productos = productoService.obtenerTodosProductos();

            model.addAttribute("error", mensajeError);
            model.addAttribute("proveedores", proveedores);
            model.addAttribute("productos", productos);
            model.addAttribute("tiposOrden", OrdenAbastecimiento.TipoOrden.values());
            model.addAttribute("ordenAbastecimiento", ordenAbastecimiento);
            model.addAttribute("modo", ordenAbastecimiento.getId() != null ? "editar" : "nueva");

            System.out.println("Formulario recargado debido a error");
            return "form-orden-abastecimiento";

        } catch (Exception e) {
            System.err.println("ERROR crítico al recargar formulario: " + e.getMessage());
            model.addAttribute("error", "Error crítico: " + e.getMessage());
            return "redirect:/ordenes-abastecimiento";
        }
    }
}