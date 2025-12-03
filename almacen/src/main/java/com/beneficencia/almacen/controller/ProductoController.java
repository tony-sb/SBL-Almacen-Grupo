package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.model.Producto;
import com.beneficencia.almacen.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para la gestión de productos del almacén.
 * Maneja las operaciones CRUD (Crear, Leer, Actualizar, Eliminar) de productos
 * a través de interfaces web utilizando el patrón MVC.
 */
@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // Lista de opciones predefinidas
    private final List<String> CATEGORIAS = Arrays.asList(
            "Medicamentos",
            "Insumos Médicos",
            "Limpieza",
            "Alimentos",
            "Material Oficina",
            "Otros"
    );
    private final List<String> UNIDADES = Arrays.asList(
            "Unidad",
            "Caja",
            "Paquete",
            "Litro",
            "Kilogramo",
            "Gramo",
            "Mililitro",
            "Frasco",
            "Tableta",
            "Cápsula",
            "Par",
            "Rollo"
    );

    /**
     * Muestra la lista de todos los productos del inventario.
     * Recupera todos los productos existentes y los pasa a la vista para su visualización.
     *
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista JSP 'productos/lista'
     */
    @GetMapping
    public String listarProductos(
            @RequestParam(value = "q", required = false) String terminoBusqueda,
            @RequestParam(value = "categoria", required = false) String categoria,
            Model model) {

        List<Producto> productos;

        if (terminoBusqueda != null && !terminoBusqueda.trim().isEmpty()) {
            productos = productoService.buscarProductosPorTermino(terminoBusqueda);
            model.addAttribute("terminoBusqueda", terminoBusqueda);
        } else if (categoria != null && !categoria.trim().isEmpty()) {
            productos = productoService.obtenerProductosPorCategoria(categoria);
            model.addAttribute("categoriaSeleccionada", categoria);
        } else {
            productos = productoService.obtenerTodosProductos();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", CATEGORIAS);
        model.addAttribute("unidades", UNIDADES);
        model.addAttribute("totalProductos", productos.size());

        return "productos/lista";
    }

    /**
     * Muestra el formulario para crear un nuevo producto.
     * Prepara el modelo con un producto vacío y las opciones predefinidas
     * para categorías y unidades de medida.
     *
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista JSP 'productos/formulario'
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", CATEGORIAS);
        model.addAttribute("unidades", UNIDADES);
        model.addAttribute("fechaHoy", LocalDate.now().plusDays(30)); // Fecha por defecto: 30 días después
        model.addAttribute("modo", "nuevo");
        return "productos/formulario";
    }

    /**
     * Procesa el formulario para guardar un nuevo producto.
     * Recibe los datos del formulario, valida y persiste el producto en la base de datos.
     * Maneja excepciones y proporciona retroalimentación al usuario mediante mensajes flash.
     *
     * @param producto Objeto Producto con los datos del formulario
     * @param redirectAttributes Atributos para mensajes flash en redirección
     * @return Redirección a la lista de productos
     */

    @PostMapping("/guardar")
    public String guardarProducto(
            @ModelAttribute Producto producto,
            @RequestParam(value = "tieneVencimiento", required = false) Boolean tieneVencimiento,
            RedirectAttributes redirectAttributes) {

        try {
            // Si no tiene vencimiento, establecer fecha como null
            if (tieneVencimiento == null || !tieneVencimiento) {
                producto.setFechaVencimiento(null);
            }

            productoService.guardarProducto(producto);
            redirectAttributes.addFlashAttribute("success",
                    "Producto " + producto.getCodigo() + " guardado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al guardar el producto: " + e.getMessage());
        }
        return "redirect:/productos";
    }

    /**
     * Muestra el formulario para editar un producto existente.
     * Busca el producto por ID y carga sus datos en el formulario para edición.
     * Si el producto no existe, redirige con un mensaje de error.
     *
     * @param id ID del producto a editar
     * @param model Modelo para pasar datos a la vista
     * @param redirectAttributes Atributos para mensajes flash en redirección
     * @return Nombre de la vista JSP 'productos/formulario' o redirección en caso de error
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarProducto(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Optional<Producto> producto = productoService.obtenerProductoPorId(id);
        if (producto.isPresent()) {
            model.addAttribute("producto", producto.get());
            model.addAttribute("categorias", CATEGORIAS);
            model.addAttribute("unidades", UNIDADES);
            model.addAttribute("tieneVencimiento", producto.get().getFechaVencimiento() != null);
            model.addAttribute("modo", "editar");
            return "productos/formulario";
        } else {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            return "redirect:/productos";
        }
    }
    /**
     * Procesa la actualización de un producto existente en el sistema.
     * Recibe los datos modificados del formulario, valida la existencia del producto,
     * aplica las modificaciones y persiste los cambios en la base de datos.
     * Maneja la lógica de fecha de vencimiento (opcional) y provee retroalimentación
     * al usuario sobre el resultado de la operación.
     *
     * @param id ID único del producto a actualizar, obtenido de la URL
     * @param producto Objeto Producto con los datos modificados del formulario
     * @param tieneVencimiento Parámetro opcional que indica si el producto tiene fecha de vencimiento
     * @param redirectAttributes Atributos para enviar mensajes flash tras la redirección
     * @return Redirección a la lista principal de productos
     * **/
    @PostMapping("/actualizar/{id}")
    public String actualizarProducto(
            @PathVariable Long id,
            @ModelAttribute Producto producto,
            @RequestParam(value = "tieneVencimiento", required = false) Boolean tieneVencimiento,
            RedirectAttributes redirectAttributes) {

        try {
            // Verificar que el producto existe
            Producto productoExistente = productoService.obtenerProductoPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

            // Si no tiene vencimiento, establecer fecha como null
            if (tieneVencimiento == null || !tieneVencimiento) {
                producto.setFechaVencimiento(null);
            }

            // Mantener el ID original
            producto.setId(id);
            productoService.actualizarProducto(producto);

            redirectAttributes.addFlashAttribute("success",
                    "Producto " + producto.getCodigo() + " actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al actualizar el producto: " + e.getMessage());
        }
        return "redirect:/productos";
    }

    /**
     * Elimina un producto del inventario.
     * Busca el producto por ID y procede con su eliminación si existe.
     * Maneja excepciones y proporciona retroalimentación al usuario.
     *
     * @param id ID del producto a eliminar
     * @param redirectAttributes Atributos para mensajes flash en redirección
     * @return Redirección a la lista de productos
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("success", "Producto eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el producto: " + e.getMessage());
        }
        return "redirect:/productos";
    }

    /**
     * Muestra la vista de detalles de un producto específico.
     * Recupera un producto por su ID único y presenta toda su información
     * en una vista de solo lectura diseñada para visualización detallada.
     *
     * @param id ID único del producto cuyos detalles se desean visualizar
     * @param model Modelo Spring para pasar datos a la vista
     * @param redirectAttributes Atributos para mensajes flash en caso de redirección
     * @return Vista 'productos/detalle' si el producto existe,
     *         redirección a la lista de productos con mensaje de error si no existe
     */
    @GetMapping("/detalle/{id}")
    public String verDetalleProducto(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Producto> producto = productoService.obtenerProductoPorId(id);
        if (producto.isPresent()) {
            model.addAttribute("producto", producto.get());
            return "productos/detalle";
        } else {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            return "redirect:/productos";
        }
    }
}