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
import java.util.*;

/**
 * Controlador para la gestión de órdenes de abastecimiento.
 * Maneja las operaciones CRUD de órdenes de compra y abastecimiento del almacén.
 */
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
     * Muestra la lista de todas las órdenes de abastecimiento (página principal).
     * Carga las órdenes existentes junto con los proveedores y productos necesarios
     * para las operaciones del formulario.
     *
     * @param model Modelo para pasar datos a la vista
     * @param authentication Información de autenticación del usuario
     * @return Nombre de la vista 'ordenes-abastecimiento'
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
     * Muestra el formulario para crear una nueva orden de abastecimiento.
     * Carga los proveedores y productos disponibles para seleccionar en la orden.
     *
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista 'form-orden-abastecimiento'
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
     * Procesa el guardado de una nueva orden de abastecimiento.
     * Valida los datos, procesa los items y asigna el usuario autenticado.
     *
     * @param ordenAbastecimiento Objeto de orden con datos básicos
     * @param proveedorId ID del proveedor seleccionado
     * @param productoIds Lista de IDs de productos incluidos en la orden
     * @param cantidades Lista de cantidades correspondientes a cada producto
     * @param precios Lista de precios unitarios correspondientes a cada producto
     * @param authentication Información de autenticación del usuario
     * @param model Modelo para pasar datos a la vista en caso de error
     * @param redirectAttributes Atributos para mensajes flash en redirección
     * @return Redirección a la lista de órdenes o recarga del formulario en caso de error
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
     * Muestra el formulario para editar una orden de abastecimiento existente.
     * Carga los datos de la orden especificada por ID para su modificación.
     *
     * @param id ID de la orden a editar
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista 'form-orden-abastecimiento' o redirección en caso de error
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
     * Procesa la actualización de una orden de abastecimiento existente.
     * Actualiza los datos básicos y reprocesa todos los items de la orden.
     *
     * @param id ID de la orden a actualizar
     * @param ordenAbastecimiento Objeto de orden con datos actualizados
     * @param proveedorId ID del proveedor seleccionado
     * @param productoIds Lista de IDs de productos actualizados
     * @param cantidades Lista de cantidades actualizadas
     * @param precios Lista de precios unitarios actualizados
     * @param authentication Información de autenticación del usuario
     * @param model Modelo para pasar datos a la vista en caso de error
     * @param redirectAttributes Atributos para mensajes flash en redirección
     * @return Redirección a la lista de órdenes o recarga del formulario en caso de error
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

            // Obtener usuario autenticado
            String username = authentication.getName();
            Usuario usuario = usuarioService.obtenerUsuarioPorUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

            // Obtener proveedor
            Proveedor proveedor = proveedorService.obtenerProveedorPorId(proveedorId)
                    .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + proveedorId));

            // Crear objeto orden con los datos actualizados
            OrdenAbastecimiento ordenActualizada = new OrdenAbastecimiento();
            ordenActualizada.setId(id); // IMPORTANTE: Mantener el ID
            ordenActualizada.setTipoOrden(ordenAbastecimiento.getTipoOrden());
            ordenActualizada.setFechaOA(ordenAbastecimiento.getFechaOA());
            ordenActualizada.setProveedor(proveedor);
            ordenActualizada.setUsuario(usuario);
            ordenActualizada.setObservaciones(ordenAbastecimiento.getObservaciones());

            // Procesar items
            List<OrdenAbastecimientoItem> items = procesarItemsParaController(productoIds, cantidades, precios);
            ordenActualizada.setItems(items);

            // IMPORTANTE: Asegurarse que cada item tenga referencia a la orden
            if (items != null) {
                for (OrdenAbastecimientoItem item : items) {
                    item.setOrdenAbastecimiento(ordenActualizada);
                }
            }

            System.out.println("📋 Datos preparados para actualización:");
            System.out.println("   - ID: " + ordenActualizada.getId());
            System.out.println("   - Items: " + (items != null ? items.size() : 0));

            // Guardar la orden actualizada
            // El service manejará la lógica de actualización
            OrdenAbastecimiento ordenGuardada = ordenAbastecimientoService.guardarOrden(ordenActualizada);
            System.out.println("✅ Orden actualizada exitosamente: " + ordenGuardada.getNumeroOA());

            // Mensaje de éxito
            redirectAttributes.addFlashAttribute("success",
                    "Orden de abastecimiento " + ordenGuardada.getNumeroOA() + " actualizada exitosamente");

            return "redirect:/ordenes-abastecimiento";

        } catch (Exception e) {
            System.err.println("❌ ERROR al actualizar orden: " + e.getMessage());
            e.printStackTrace();

            return recargarFormularioConError(model, ordenAbastecimiento,
                    "Error al actualizar la orden: " + e.getMessage());
        }
    }

    /**
     * Método auxiliar para procesar items en el controller
     */
    private List<OrdenAbastecimientoItem> procesarItemsParaController(List<Long> productoIds,
                                                                      List<Integer> cantidades,
                                                                      List<BigDecimal> precios) {

        if (productoIds == null || productoIds.isEmpty() || productoIds.stream().allMatch(Objects::isNull)) {
            System.out.println("⚠️  No hay items para procesar en el controller");
            return new ArrayList<>();
        }

        List<OrdenAbastecimientoItem> items = new ArrayList<>();
        Set<Long> productosYaProcesados = new HashSet<>();
        int itemsValidos = 0;

        for (int i = 0; i < productoIds.size(); i++) {
            Long productoId = productoIds.get(i);
            Integer cantidad = cantidades != null && i < cantidades.size() ? cantidades.get(i) : null;
            BigDecimal precio = precios != null && i < precios.size() ? precios.get(i) : null;

            // Validar datos
            if (productoId != null && cantidad != null && precio != null &&
                    productoId > 0 && cantidad > 0 && precio.compareTo(BigDecimal.ZERO) >= 0) {

                // Evitar duplicados
                if (productosYaProcesados.contains(productoId)) {
                    System.out.println("⚠️  Producto duplicado ignorado: ID " + productoId);
                    continue;
                }

                try {
                    Producto producto = productoService.obtenerProductoPorId(productoId)
                            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado ID: " + productoId));

                    OrdenAbastecimientoItem item = new OrdenAbastecimientoItem();
                    item.setProducto(producto);
                    item.setCantidad(cantidad);
                    item.setPrecioUnitario(precio);
                    item.setSubtotal(precio.multiply(BigDecimal.valueOf(cantidad)));
                    // NOTA: NO establecer ordenAbastecimiento aquí, se hará en el controller principal

                    items.add(item);
                    productosYaProcesados.add(productoId);
                    itemsValidos++;

                    System.out.println("✅ Item preparado: " + producto.getNombre() +
                            " x " + cantidad + " = S/ " + item.getSubtotal());

                } catch (Exception e) {
                    System.err.println("Error procesando item " + i + ": " + e.getMessage());
                }
            }
        }

        if (itemsValidos == 0 && productoIds != null && !productoIds.isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un producto válido a la orden");
        }

        System.out.println("📦 Total items preparados en controller: " + itemsValidos);
        return items;
    }
    /**
     * Elimina una orden de abastecimiento del sistema.
     * Realiza validaciones antes de proceder con la eliminación.
     *
     * @param id ID de la orden a eliminar
     * @param redirectAttributes Atributos para mensajes flash en redirección
     * @return Redirección a la lista de órdenes
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
     * Muestra la vista optimizada para imprimir una orden de abastecimiento.
     * Carga todos los datos de la orden incluyendo items para generar un formato imprimible.
     *
     * @param id ID de la orden a imprimir
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista 'imprimir-orden-abastecimiento' o redirección en caso de error
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
     * Procesa los items para una nueva orden de abastecimiento.
     * Valida y crea los items basados en los IDs de producto, cantidades y precios proporcionados.
     *
     * @param ordenAbastecimiento Orden a la que se agregarán los items
     * @param productoIds Lista de IDs de productos
     * @param cantidades Lista de cantidades correspondientes
     * @param precios Lista de precios unitarios correspondientes
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
     * Procesa items para edición de una orden existente.
     * Limpia los items actuales y agrega los nuevos items proporcionados.
     *
     * @param ordenExistente Orden existente a actualizar
     * @param productoIds Lista de IDs de productos actualizados
     * @param cantidades Lista de cantidades actualizadas
     * @param precios Lista de precios unitarios actualizados
     */
    private void procesarItemsParaEdicion(OrdenAbastecimiento ordenExistente,
                                          List<Long> productoIds,
                                          List<Integer> cantidades,
                                          List<BigDecimal> precios) {

        // CORRECCIÓN: Usar el repositorio para eliminar items en lugar de solo limpiar la lista
        // Esto evita el error de FK constraint
        if (ordenExistente.getItems() != null && !ordenExistente.getItems().isEmpty()) {
            // IMPORTANTE: Crear una copia de la lista para evitar ConcurrentModificationException
            List<OrdenAbastecimientoItem> itemsAEliminar = new ArrayList<>(ordenExistente.getItems());

            // Romper la relación bidireccional antes de eliminar
            for (OrdenAbastecimientoItem item : itemsAEliminar) {
                item.setOrdenAbastecimiento(null);
                ordenExistente.getItems().remove(item);
            }

            System.out.println("✅ Items existentes eliminados correctamente: " + itemsAEliminar.size());
        } else {
            ordenExistente.setItems(new ArrayList<>());
        }

        // Procesar nuevos items solo si hay datos válidos
        if (productoIds != null && cantidades != null && precios != null) {

            List<OrdenAbastecimientoItem> nuevosItems = new ArrayList<>();
            int itemsValidos = 0;

            // Usar un Set para evitar duplicados por producto
            Set<Long> productosYaAgregados = new HashSet<>();

            for (int i = 0; i < productoIds.size(); i++) {
                Long productoId = productoIds.get(i);
                Integer cantidad = cantidades.get(i);
                BigDecimal precio = precios.get(i);

                // Validar que todos los datos del item son válidos
                if (productoId != null && cantidad != null && precio != null &&
                        productoId > 0 && cantidad > 0 && precio.compareTo(BigDecimal.ZERO) >= 0) {

                    try {
                        // Verificar que no sea un duplicado en los nuevos items
                        if (productosYaAgregados.contains(productoId)) {
                            System.out.println("⚠️  Producto duplicado ignorado: ID " + productoId);
                            continue;
                        }

                        Producto producto = productoService.obtenerProductoPorId(productoId)
                                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado ID: " + productoId));

                        OrdenAbastecimientoItem item = new OrdenAbastecimientoItem();
                        item.setProducto(producto);
                        item.setCantidad(cantidad);
                        item.setPrecioUnitario(precio);
                        item.setSubtotal(precio.multiply(BigDecimal.valueOf(cantidad)));
                        item.setOrdenAbastecimiento(ordenExistente); // IMPORTANTE: Establecer la relación

                        nuevosItems.add(item);
                        productosYaAgregados.add(productoId);
                        itemsValidos++;

                        System.out.println("✅ Item procesado: " + producto.getNombre() +
                                " x " + cantidad + " = S/ " + item.getSubtotal());

                    } catch (Exception e) {
                        System.err.println("❌ Error procesando item " + i + " para edición: " + e.getMessage());
                    }
                }
            }

            if (itemsValidos == 0 && (productoIds.size() > 0 && !productoIds.stream().allMatch(Objects::isNull))) {
                throw new IllegalArgumentException("Debe agregar al menos un producto válido a la orden");
            }

            // Agregar los nuevos items a la orden
            ordenExistente.getItems().addAll(nuevosItems);
            System.out.println("📦 Total items actualizados: " + itemsValidos);
        } else {
            System.out.println("ℹ️  No hay items para actualizar");
            if (productoIds != null && productoIds.size() > 0) {
                throw new IllegalArgumentException("Los datos de los productos no son válidos");
            }
        }
    }
    /**
     * Recarga el formulario con los datos actuales y un mensaje de error.
     * Utilizado cuando ocurre un error durante el guardado o actualización.
     *
     * @param model Modelo para pasar datos a la vista
     * @param ordenAbastecimiento Orden con datos actuales
     * @param mensajeError Mensaje de error a mostrar
     * @return Nombre de la vista 'form-orden-abastecimiento' con datos recargados
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