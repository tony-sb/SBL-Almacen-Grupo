package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.model.*;
import com.beneficencia.almacen.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/ordenes-compra")
public class OrdenCompraController {

    @Autowired
    private OrdenCompraService ordenCompraService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String listarOrdenesCompra(Model model, Authentication authentication) {
        try {
            System.out.println("=== CARGANDO ÓRDENES DE COMPRA ===");

            List<OrdenCompra> ordenes = ordenCompraService.obtenerTodasOrdenes();
            List<Proveedor> proveedores = proveedorService.obtenerTodosProveedores();
            List<Producto> productos = productoService.obtenerTodosProductos();

            System.out.println("Órdenes encontradas: " + ordenes.size());
            System.out.println("Proveedores encontrados: " + proveedores.size());
            System.out.println("Productos encontrados: " + productos.size());

            model.addAttribute("ordenes", ordenes);
            model.addAttribute("proveedores", proveedores);
            model.addAttribute("productos", productos);
            model.addAttribute("tiposOrden", OrdenCompra.TipoOrden.values());
            model.addAttribute("ordenCompra", new OrdenCompra());

            return "ordenes-compra";
        } catch (Exception e) {
            System.err.println("ERROR al cargar órdenes: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar los datos: " + e.getMessage());
            model.addAttribute("ordenes", new ArrayList<OrdenCompra>());
            model.addAttribute("proveedores", new ArrayList<Proveedor>());
            model.addAttribute("productos", new ArrayList<Producto>());
            model.addAttribute("tiposOrden", OrdenCompra.TipoOrden.values());
            return "ordenes-compra";
        }
    }

    @PostMapping("/guardar")
    public String guardarOrdenCompra(@ModelAttribute OrdenCompra ordenCompra,
                                     @RequestParam Long proveedorId,
                                     @RequestParam(required = false) List<Long> productoIds,
                                     @RequestParam(required = false) List<Integer> cantidades,
                                     @RequestParam(required = false) List<BigDecimal> precios,
                                     Authentication authentication,
                                     Model model) {
        try {
            System.out.println("=== GUARDANDO NUEVA ORDEN ===");
            System.out.println("Tipo orden: " + ordenCompra.getTipoOrden());
            System.out.println("Proveedor ID: " + proveedorId);

            // Obtener el usuario autenticado
            String username = authentication.getName();
            Usuario usuario = usuarioService.obtenerUsuarioPorUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

            System.out.println("Usuario autenticado: " + usuario.getNombre());

            // Establecer proveedor
            Proveedor proveedor = proveedorService.obtenerProveedorPorId(proveedorId)
                    .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
            ordenCompra.setProveedor(proveedor);
            System.out.println("Proveedor: " + proveedor.getNombre());

            // Establecer usuario
            ordenCompra.setUsuario(usuario);

            // Procesar items si existen
            if (productoIds != null && cantidades != null && precios != null &&
                    productoIds.size() == cantidades.size() && productoIds.size() == precios.size()) {

                List<OrdenCompraItem> items = new ArrayList<>();
                for (int i = 0; i < productoIds.size(); i++) {
                    if (productoIds.get(i) != null && cantidades.get(i) != null && precios.get(i) != null) {
                        int finalI = i;
                        Producto producto = productoService.obtenerProductoPorId(productoIds.get(i))
                                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado ID: " + productoIds.get(finalI)));

                        OrdenCompraItem item = new OrdenCompraItem();
                        item.setProducto(producto);
                        item.setCantidad(cantidades.get(i));
                        item.setPrecioUnitario(precios.get(i));
                        item.setSubtotal(precios.get(i).multiply(BigDecimal.valueOf(cantidades.get(i))));
                        item.setOrdenCompra(ordenCompra);
                        items.add(item);

                        System.out.println("Item agregado: " + producto.getNombre() + " x " + cantidades.get(i) + " = S/ " + item.getSubtotal());
                    }
                }
                ordenCompra.setItems(items);
                System.out.println("Total items: " + items.size());
            } else {
                System.out.println("No hay items para procesar");
                ordenCompra.setItems(new ArrayList<>());
            }

            // Guardar la orden
            OrdenCompra ordenGuardada = ordenCompraService.guardarOrden(ordenCompra);
            System.out.println("Orden guardada exitosamente: " + ordenGuardada.getNumeroOC());

            return "redirect:/ordenes-compra?success=true";

        } catch (Exception e) {
            System.err.println("ERROR al guardar orden: " + e.getMessage());
            e.printStackTrace();

            // Recargar datos para mostrar el formulario nuevamente
            List<Proveedor> proveedores = proveedorService.obtenerTodosProveedores();
            List<Producto> productos = productoService.obtenerTodosProductos();
            List<OrdenCompra> ordenes = ordenCompraService.obtenerTodasOrdenes();

            model.addAttribute("error", "Error al guardar la orden: " + e.getMessage());
            model.addAttribute("proveedores", proveedores);
            model.addAttribute("productos", productos);
            model.addAttribute("ordenes", ordenes);
            model.addAttribute("tiposOrden", OrdenCompra.TipoOrden.values());
            model.addAttribute("ordenCompra", ordenCompra);

            return "ordenes-compra";
        }
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNuevaOrden(Model model) {
        model.addAttribute("ordenCompra", new OrdenCompra());
        model.addAttribute("proveedores", proveedorService.obtenerTodosProveedores());
        model.addAttribute("productos", productoService.obtenerTodosProductos());
        model.addAttribute("tiposOrden", OrdenCompra.TipoOrden.values());
        return "form-orden-compra";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarOrden(@PathVariable Long id, Model model) {
        try {
            OrdenCompra ordenCompra = ordenCompraService.obtenerOrdenPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + id));

            // Forzar carga de items
            if (ordenCompra.getItems() != null) {
                ordenCompra.getItems().size();
            }

            List<Proveedor> proveedores = proveedorService.obtenerTodosProveedores();
            List<Producto> productos = productoService.obtenerTodosProductos();

            model.addAttribute("ordenCompra", ordenCompra);
            model.addAttribute("proveedores", proveedores);
            model.addAttribute("productos", productos);
            model.addAttribute("tiposOrden", OrdenCompra.TipoOrden.values());

            return "form-orden-compra";
        } catch (Exception e) {
            System.err.println("Error al cargar orden para editar: " + e.getMessage());
            model.addAttribute("error", "Error al cargar la orden: " + e.getMessage());
            return "redirect:/ordenes-compra";
        }
    }

    @PostMapping("/editar/{id}")
    public String actualizarOrdenCompra(@PathVariable Long id,
                                        @ModelAttribute OrdenCompra ordenCompra,
                                        @RequestParam Long proveedorId,
                                        @RequestParam(required = false) List<Long> productoIds,
                                        @RequestParam(required = false) List<Integer> cantidades,
                                        @RequestParam(required = false) List<BigDecimal> precios,
                                        Authentication authentication,
                                        Model model) {
        try {
            System.out.println("=== ACTUALIZANDO ORDEN EXISTENTE ===");
            System.out.println("Orden ID: " + id);

            // Verificar que la orden existe
            OrdenCompra ordenExistente = ordenCompraService.obtenerOrdenPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + id));

            // Obtener el usuario autenticado
            String username = authentication.getName();
            Usuario usuario = usuarioService.obtenerUsuarioPorUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

            // Establecer proveedor
            Proveedor proveedor = proveedorService.obtenerProveedorPorId(proveedorId)
                    .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));

            // Actualizar datos básicos
            ordenExistente.setTipoOrden(ordenCompra.getTipoOrden());
            ordenExistente.setFechaOC(ordenCompra.getFechaOC());
            ordenExistente.setProveedor(proveedor);
            ordenExistente.setObservaciones(ordenCompra.getObservaciones());
            ordenExistente.setFechaActualizacion(LocalDateTime.now());

            // Limpiar items existentes
            ordenExistente.getItems().clear();

            // Procesar nuevos items
            if (productoIds != null && cantidades != null && precios != null &&
                    productoIds.size() == cantidades.size() && productoIds.size() == precios.size()) {

                List<OrdenCompraItem> items = new ArrayList<>();
                for (int i = 0; i < productoIds.size(); i++) {
                    if (productoIds.get(i) != null && cantidades.get(i) != null && precios.get(i) != null) {
                        int finalI = i;
                        Producto producto = productoService.obtenerProductoPorId(productoIds.get(i))
                                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado ID: " + productoIds.get(finalI)));

                        OrdenCompraItem item = new OrdenCompraItem();
                        item.setProducto(producto);
                        item.setCantidad(cantidades.get(i));
                        item.setPrecioUnitario(precios.get(i));
                        item.setSubtotal(precios.get(i).multiply(BigDecimal.valueOf(cantidades.get(i))));
                        item.setOrdenCompra(ordenExistente);
                        items.add(item);

                        System.out.println("Item actualizado: " + producto.getNombre() + " x " + cantidades.get(i));
                    }
                }
                ordenExistente.getItems().addAll(items);
                System.out.println("Total items actualizados: " + items.size());
            }

            // Guardar la orden actualizada
            OrdenCompra ordenActualizada = ordenCompraService.guardarOrden(ordenExistente);
            System.out.println("Orden actualizada exitosamente: " + ordenActualizada.getNumeroOC());

            return "redirect:/ordenes-compra?success=updated";

        } catch (Exception e) {
            System.err.println("ERROR al actualizar orden: " + e.getMessage());
            e.printStackTrace();

            // Recargar datos para mostrar el formulario nuevamente
            List<Proveedor> proveedores = proveedorService.obtenerTodosProveedores();
            List<Producto> productos = productoService.obtenerTodosProductos();

            model.addAttribute("error", "Error al actualizar la orden: " + e.getMessage());
            model.addAttribute("proveedores", proveedores);
            model.addAttribute("productos", productos);
            model.addAttribute("tiposOrden", OrdenCompra.TipoOrden.values());
            model.addAttribute("ordenCompra", ordenCompra);

            return "form-orden-compra";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarOrdenCompra(@PathVariable Long id) {
        try {
            ordenCompraService.eliminarOrden(id);
            System.out.println("Orden eliminada: " + id);
        } catch (Exception e) {
            System.err.println("Error al eliminar orden: " + e.getMessage());
        }
        return "redirect:/ordenes-compra";
    }

    @GetMapping("/imprimir/{id}")
    public String imprimirOrdenCompra(@PathVariable Long id, Model model) {
        try {
            OrdenCompra ordenCompra = ordenCompraService.obtenerOrdenPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + id));

            // Forzar la carga de los items (LAZY loading)
            if (ordenCompra.getItems() != null) {
                ordenCompra.getItems().size();
            }

            model.addAttribute("ordenCompra", ordenCompra);
            return "imprimir-orden-compra";
        } catch (Exception e) {
            System.err.println("Error al cargar orden para imprimir: " + e.getMessage());
            model.addAttribute("error", "Error al cargar la orden: " + e.getMessage());
            return "redirect:/ordenes-compra";
        }
    }
}