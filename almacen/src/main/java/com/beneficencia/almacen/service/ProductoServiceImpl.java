package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.Producto;
import com.beneficencia.almacen.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    private static final Map<String, String> PREFIJOS_CATEGORIA = Map.of(
            "Medicamentos", "MED",
            "Insumos Médicos", "INS",
            "Limpieza", "LIM",
            "Alimentos", "ALI",
            "Material Oficina", "OFI",
            "Otros", "OTR"
    );

    private static final String PREFIJO_DEFAULT = "PROD";

    private String generarCodigoAutomatico(String categoria) {

        String prefijo = PREFIJOS_CATEGORIA.getOrDefault(categoria, PREFIJO_DEFAULT);

        List<Producto> productos = productoRepository.findByCodigoStartingWithOrderByIdDesc(prefijo + "-");

        int siguienteNumero = 1;
        if (!productos.isEmpty()) {
            String ultimoCodigo = productos.get(0).getCodigo();
            try {
                String[] partes = ultimoCodigo.split("-");
                if (partes.length > 1) {
                    String numeroStr = partes[1].replaceAll("^0+", "");
                    siguienteNumero = Integer.parseInt(numeroStr.isEmpty() ? "0" : numeroStr) + 1;
                }
            } catch (NumberFormatException e) {
                siguienteNumero = 1;
            }
        }

        return String.format("%s-%03d", prefijo, siguienteNumero);
    }

    public boolean codigoExiste(String codigo) {
        return productoRepository.existsByCodigoIgnoreCase(codigo);
    }

    @Override
    public List<Producto> obtenerTodosProductos() {
        return productoRepository.findAll();
    }

    @Override
    public Optional<Producto> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    @Override
    public Producto guardarProducto(Producto producto) {
        System.out.println("Guardando producto - Fecha vencimiento: " + producto.getFechaVencimiento());

        if (producto.getCodigo() == null || producto.getCodigo().trim().isEmpty()) {
            String codigoAutomatico = generarCodigoAutomatico(producto.getCategoria());
            producto.setCodigo(codigoAutomatico);
            System.out.println("Código generado automáticamente: " + codigoAutomatico);
        } else {
            if (codigoExiste(producto.getCodigo())) {
                throw new IllegalArgumentException("El código " + producto.getCodigo() + " ya existe");
            }
        }

        System.out.println("Producto completo: " + producto);
        return productoRepository.save(producto);
    }

    @Override
    public Producto actualizarProducto(Producto producto) {
        // Para actualización, no generamos nuevo código
        return productoRepository.save(producto);
    }

    @Override
    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }

    @Override
    public List<Producto> obtenerProductosPorCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    @Override
    public List<Producto> obtenerProductosConStockBajo() {
        return productoRepository.findProductosConStockBajo();
    }

    @Override
    public boolean existeProductoPorCodigo(String codigo) {
        return productoRepository.existsByCodigo(codigo);
    }

    @Override
    public List<Producto> buscarProductosPorTermino(String termino) {

        try {
            return productoRepository.findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(termino, termino);
        } catch (Exception e) {

            List<Producto> todosProductos = productoRepository.findAll();
            return todosProductos.stream()
                    .filter(producto ->
                            (producto.getCodigo() != null && producto.getCodigo().toLowerCase().contains(termino.toLowerCase())) ||
                                    (producto.getNombre() != null && producto.getNombre().toLowerCase().contains(termino.toLowerCase())))
                    .toList();
        }
    }

    @Override
    public Optional<Producto> obtenerProductoPorCodigo(String codigo) {

        try {
            return productoRepository.findByCodigo(codigo);
        } catch (Exception e) {

            return productoRepository.findAll().stream()
                    .filter(producto -> producto.getCodigo() != null && producto.getCodigo().equalsIgnoreCase(codigo))
                    .findFirst();
        }
    }

    @Override
    public Long contarProductosConStockBajo() {
        try {
            return productoRepository.countByCantidadLessThanEqualStockMinimo();
        } catch (Exception e) {
            // Fallback a implementación manual
            return productoRepository.findAll().stream()
                    .filter(this::tieneStockBajo)
                    .count();
        }
    }

    @Override
    public List<String> obtenerTodasLasCategorias() {
        return productoRepository.findAll().stream()
                .map(Producto::getCategoria)
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    public boolean tieneStockBajo(Producto producto) {
        if (producto.getCantidad() == null) return true;
        if (producto.getStockMinimo() == null) return producto.getCantidad() <= 5;
        return producto.getCantidad() <= producto.getStockMinimo();
    }

    @Override
    public Map<String, Object> obtenerEstadisticasInventario() {
        List<Producto> todosProductos = productoRepository.findAll();
        List<Producto> productosStockBajo = obtenerProductosConStockBajo();
        List<String> categorias = obtenerTodasLasCategorias();

        Integer totalItems = todosProductos.stream()
                .mapToInt(producto -> producto.getCantidad() != null ? producto.getCantidad() : 0)
                .sum();

        Double valorTotal = todosProductos.stream()
                .mapToDouble(producto ->
                        producto.getPrecioUnitario() != null && producto.getCantidad() != null ?
                                producto.getPrecioUnitario().doubleValue() * producto.getCantidad() : 0
                )
                .sum();

        Map<String, Long> productosPorCategoria = todosProductos.stream()
                .collect(HashMap::new,
                        (map, producto) -> map.merge(producto.getCategoria(), 1L, Long::sum),
                        HashMap::putAll);

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalProductos", todosProductos.size());
        estadisticas.put("totalItems", totalItems);
        estadisticas.put("productosStockBajo", productosStockBajo.size());
        estadisticas.put("totalCategorias", categorias.size());
        estadisticas.put("valorTotalInventario", valorTotal);
        estadisticas.put("productosPorCategoria", productosPorCategoria);

        return estadisticas;
    }
}