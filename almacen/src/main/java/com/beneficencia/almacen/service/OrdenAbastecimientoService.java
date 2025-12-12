package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.*;
import com.beneficencia.almacen.repository.OrdenAbastecimientoItemRepository;
import com.beneficencia.almacen.repository.OrdenAbastecimientoRepository;
import com.beneficencia.almacen.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrdenAbastecimientoService {

    @Autowired
    private OrdenAbastecimientoRepository ordenAbastecimientoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private OrdenAbastecimientoItemRepository ordenAbastecimientoItemRepository;

    @Autowired  //
    private ProductoService productoService;

    @PersistenceContext
    private EntityManager entityManager;

    private void gestionarItemsAntiguos(OrdenAbastecimiento ordenActualizada) {
        try {
            System.out.println("Gestionando items antiguos para orden ID: " + ordenActualizada.getId());

            OrdenAbastecimiento ordenExistente = ordenAbastecimientoRepository
                    .findByIdWithItems(ordenActualizada.getId())
                    .orElseThrow(() -> new RuntimeException("Orden no encontrada: " + ordenActualizada.getId()));

            if (ordenExistente.getItems() != null && !ordenExistente.getItems().isEmpty()) {
                System.out.println("Eliminando " + ordenExistente.getItems().size() + " items antiguos");

                List<OrdenAbastecimientoItem> itemsAEliminar = new ArrayList<>(ordenExistente.getItems());

                for (OrdenAbastecimientoItem item : itemsAEliminar) {

                    item.setOrdenAbastecimiento(null);

                    ordenExistente.getItems().remove(item);

                    if (item.getId() != null) {
                        ordenAbastecimientoItemRepository.deleteById(item.getId());
                    }
                }

                System.out.println("Items antiguos eliminados correctamente");
            }

        } catch (Exception e) {
            System.err.println("Error gestionando items antiguos: " + e.getMessage());
            throw new RuntimeException("Error al gestionar items antiguos", e);
        }
    }

    public List<OrdenAbastecimiento> obtenerTodasOrdenes() {
        try {
            System.out.println("=== BUSCANDO TODAS LAS ÓRDENES DE ABASTECIMIENTO ===");
            List<OrdenAbastecimiento> ordenes = ordenAbastecimientoRepository.findAllWithProveedorAndUsuario();
            System.out.println("Órdenes encontradas: " + ordenes.size());

            for (OrdenAbastecimiento orden : ordenes) {
                System.out.println(" []  " + orden.getNumeroOA() +
                        " - " + orden.getTipoOrden() +
                        " - " + orden.getEstado() +
                        " - Proveedor: " + (orden.getProveedor() != null ? orden.getProveedor().getNombre() : "null") +
                        " - Usuario: " + (orden.getUsuario() != null ? orden.getUsuario().getNombre() : "null"));
            }

            return ordenes;
        } catch (Exception e) {
            System.err.println("ERROR en consulta principal: " + e.getMessage());
            e.printStackTrace();

            System.out.println("=== USANDO MÉTODO SIMPLE COMO RESPUESTA ===");

            try {
                List<OrdenAbastecimiento> ordenes = ordenAbastecimientoRepository.findAll();
                System.out.println("Órdenes con método simple: " + ordenes.size());
                return ordenes;
            } catch (Exception ex) {
                System.err.println("ERROR crítico: " + ex.getMessage());
                throw new RuntimeException("Error al cargar órdenes de abastecimiento", ex);
            }
        }
    }

    public Optional<OrdenAbastecimiento> obtenerOrdenPorId(Long id) {
        try {
            System.out.println("=== BUSCANDO ORDEN POR ID: " + id + " ===");
            Optional<OrdenAbastecimiento> orden = ordenAbastecimientoRepository.findByIdWithItems(id);

            if (orden.isPresent()) {
                OrdenAbastecimiento ordenEncontrada = orden.get();

                if (ordenEncontrada.getItems() != null) {
                    List<OrdenAbastecimientoItem> itemsValidos = ordenEncontrada.getItems().stream()
                            .filter(item -> item.getProducto() != null) // ✅ FILTRAR PRODUCTOS NULOS
                            .collect(Collectors.toList());

                    ordenEncontrada.setItems(itemsValidos);
                    System.out.println("Items después de filtrar nulos: " + itemsValidos.size());
                }

                System.out.println("Orden encontrada: " + ordenEncontrada.getNumeroOA());
                return Optional.of(ordenEncontrada);
            } else {
                System.out.println("Orden no encontrada con ID: " + id);
                return Optional.empty();
            }
        } catch (Exception e) {
            System.err.println("ERROR cargando orden con items: " + e.getMessage());
            e.printStackTrace();
            return ordenAbastecimientoRepository.findById(id);
        }
    }

    @Transactional
    public OrdenAbastecimiento guardarOrden(OrdenAbastecimiento ordenAbastecimiento) {
        try {
            System.out.println("=== INICIANDO GUARDADO DE ORDEN DE ABASTECIMIENTO ===");

            boolean esNuevaOrden = (ordenAbastecimiento.getId() == null);

            System.out.println("¿Es nueva orden? " + esNuevaOrden +
                    " - ID: " + ordenAbastecimiento.getId());

            if (!esNuevaOrden) {
                eliminarItemsAntiguosDirectamente(ordenAbastecimiento.getId());
            }

            if (ordenAbastecimiento.getId() != null) {
                eliminarItemsAntiguosDirectamente(ordenAbastecimiento.getId());
            }

            if (ordenAbastecimiento.getTipoOrden() == null) {
                throw new IllegalArgumentException("El tipo de orden es requerido");
            }
            if (ordenAbastecimiento.getProveedor() == null) {
                throw new IllegalArgumentException("El proveedor es requerido");
            }

            if (ordenAbastecimiento.getNumeroOA() == null || ordenAbastecimiento.getNumeroOA().isEmpty()) {
                String nuevoNumero = generarNumeroOAUnico(ordenAbastecimiento.getTipoOrden());
                ordenAbastecimiento.setNumeroOA(nuevoNumero);
                System.out.println("Número de orden generado: " + nuevoNumero);
            }

            if (ordenAbastecimiento.getItems() != null) {
                for (OrdenAbastecimientoItem item : ordenAbastecimiento.getItems()) {
                    item.setOrdenAbastecimiento(ordenAbastecimiento);
                }
            }

            calcularTotal(ordenAbastecimiento);

            if (ordenAbastecimiento.getFechaCreacion() == null) {
                ordenAbastecimiento.setFechaCreacion(LocalDateTime.now());
                System.out.println("Fecha de creación establecida");
            }
            ordenAbastecimiento.setFechaActualizacion(LocalDateTime.now());

            if (ordenAbastecimiento.getEstado() == null) {
                ordenAbastecimiento.setEstado(OrdenAbastecimiento.EstadoOrden.PENDIENTE);
                System.out.println("Estado establecido: PENDIENTE");
            }

            validarItemsOrden(ordenAbastecimiento);

            System.out.println("Guardando orden con número: " + ordenAbastecimiento.getNumeroOA());

            OrdenAbastecimiento ordenGuardada = ordenAbastecimientoRepository.save(ordenAbastecimiento);

            actualizarInventarioProductos(ordenGuardada, esNuevaOrden);

            System.out.println("Orden guardada exitosamente con ID: " + ordenGuardada.getId());
            System.out.println("Total de orden: S/ " + ordenGuardada.getTotal());
            System.out.println("Items en orden guardada: " +
                    (ordenGuardada.getItems() != null ? ordenGuardada.getItems().size() : 0));

            return ordenGuardada;

        } catch (Exception e) {
            System.err.println("ERROR al guardar orden: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al guardar la orden: " + e.getMessage(), e);
        }
    }

    private void actualizarInventarioProductos(OrdenAbastecimiento orden, boolean esNuevaOrden) {
        System.out.println("ACTUALIZANDO INVENTARIO - Orden: " + orden.getNumeroOA() +
                " - Tipo: " + (esNuevaOrden ? "NUEVA" : "EDITAR"));

        if (orden.getItems() == null || orden.getItems().isEmpty()) {
            System.out.println("Orden sin items, no hay inventario que actualizar");
            return;
        }

        if (esNuevaOrden) {
            System.out.println("Sumando items al inventario (orden nueva)");
            sumarAlInventario(orden);
        } else {
            System.out.println("Ajustando inventario (orden editada)");
            ajustarInventarioPorEdicion(orden);
        }
    }

    private void sumarAlInventario(OrdenAbastecimiento orden) {
        for (OrdenAbastecimientoItem item : orden.getItems()) {
            try {
                Producto producto = item.getProducto();
                Integer cantidadOrdenada = item.getCantidad();
                BigDecimal precioOrdenado = item.getPrecioUnitario();

                if (producto != null && cantidadOrdenada != null && cantidadOrdenada > 0) {
                    Producto productoActual = productoService.obtenerProductoPorId(producto.getId())
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + producto.getId()));

                    Integer cantidadActual = productoActual.getCantidad() != null ? productoActual.getCantidad() : 0;
                    Integer nuevaCantidad = cantidadActual + cantidadOrdenada;
                    productoActual.setCantidad(nuevaCantidad);

                    if (precioOrdenado != null && precioOrdenado.compareTo(BigDecimal.ZERO) > 0) {
                        productoActual.setPrecioUnitario(precioOrdenado);
                    }

                    productoService.actualizarProducto(productoActual);

                    System.out.println("Producto sumado: " + productoActual.getNombre() +
                            " - Stock: " + cantidadActual + " + " + cantidadOrdenada + " = " + nuevaCantidad);
                }
            } catch (Exception e) {
                System.err.println("Error sumando producto: " + e.getMessage());
            }
        }
    }

    private void ajustarInventarioPorEdicion(OrdenAbastecimiento orden) {
        try {
            OrdenAbastecimiento ordenOriginal = ordenAbastecimientoRepository
                    .findByIdWithItems(orden.getId())
                    .orElseThrow(() -> new RuntimeException("Orden original no encontrada: " + orden.getId()));

            System.out.println("Comparando orden editada con original:");
            System.out.println("   - Original: " + (ordenOriginal.getItems() != null ? ordenOriginal.getItems().size() : 0) + " items");
            System.out.println("   - Editada: " + (orden.getItems() != null ? orden.getItems().size() : 0) + " items");

            Map<Long, Integer> itemsOriginales = new HashMap<>();
            if (ordenOriginal.getItems() != null) {
                for (OrdenAbastecimientoItem item : ordenOriginal.getItems()) {
                    if (item.getProducto() != null) {
                        itemsOriginales.put(item.getProducto().getId(), item.getCantidad());
                        System.out.println("Original - Producto " + item.getProducto().getId() +
                                ": " + item.getCantidad() + " unidades");
                    }
                }
            }

            for (OrdenAbastecimientoItem itemEditado : orden.getItems()) {
                try {
                    if (itemEditado.getProducto() == null || itemEditado.getCantidad() == null) continue;

                    Long productoId = itemEditado.getProducto().getId();
                    Integer cantidadEditada = itemEditado.getCantidad();
                    BigDecimal precioEditado = itemEditado.getPrecioUnitario();

                    Producto productoActual = productoService.obtenerProductoPorId(productoId)
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));

                    Integer cantidadActual = productoActual.getCantidad() != null ? productoActual.getCantidad() : 0;
                    Integer cantidadOriginal = itemsOriginales.getOrDefault(productoId, 0);

                    Integer diferencia = cantidadEditada - cantidadOriginal;

                    System.out.println("Producto " + productoId + " - " + productoActual.getNombre() +
                            ": Original=" + cantidadOriginal +
                            ", Editado=" + cantidadEditada +
                            ", Diferencia=" + diferencia +
                            ", Stock actual=" + cantidadActual);

                    if (diferencia != 0) {
                        Integer nuevaCantidad = cantidadActual + diferencia;

                        if (nuevaCantidad < 0) {
                            System.err.println("ADVERTENCIA: Stock negativo para " + productoActual.getNombre() +
                                    " - Diferencia: " + diferencia);
                            nuevaCantidad = 0;
                        }

                        productoActual.setCantidad(nuevaCantidad);

                        System.out.println("Ajuste: " + cantidadActual + " + " + diferencia + " = " + nuevaCantidad);
                    }

                    if (precioEditado != null && precioEditado.compareTo(BigDecimal.ZERO) > 0) {
                        productoActual.setPrecioUnitario(precioEditado);
                        System.out.println("Precio actualizado: " + productoActual.getPrecioUnitario());
                    }

                    productoService.actualizarProducto(productoActual);

                } catch (Exception e) {
                    System.err.println("Error ajustando producto: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error al obtener orden original: " + e.getMessage());
            System.out.println("Fallback: Tratando como nueva orden");
            sumarAlInventario(orden);
        }
    }

    private void eliminarItemsAntiguosDirectamente(Long ordenId) {
        try {
            System.out.println("ELIMINANDO items antiguos para orden ID: " + ordenId);

            String deleteQuery = "DELETE FROM OrdenAbastecimientoItem i WHERE i.ordenAbastecimiento.id = :ordenId";
            int deleted = entityManager.createQuery(deleteQuery)
                    .setParameter("ordenId", ordenId)
                    .executeUpdate();

            System.out.println(" - " + deleted + " items antiguos ELIMINADOS (no set null)");

            entityManager.flush();
            entityManager.clear();

        } catch (Exception e) {
            System.err.println("Error eliminando items antiguos: " + e.getMessage());
            throw new RuntimeException("Error al eliminar items antiguos", e);
        }
    }

    @Transactional
    public void eliminarOrden(Long id) {
        try {
            System.out.println("=== ELIMINANDO ORDEN - ID: " + id + " ===");

            if (!ordenAbastecimientoRepository.existsById(id)) {
                throw new IllegalArgumentException("Orden no encontrada con ID: " + id);
            }

            ordenAbastecimientoRepository.deleteById(id);
            System.out.println("Orden eliminada correctamente");

        } catch (Exception e) {
            System.err.println("ERROR al eliminar orden: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar la orden: " + e.getMessage(), e);
        }
    }

    public List<OrdenAbastecimiento> obtenerOrdenesPorTipo(OrdenAbastecimiento.TipoOrden tipoOrden) {
        try {
            System.out.println("=== BUSCANDO ÓRDENES POR TIPO: " + tipoOrden + " ===");
            List<OrdenAbastecimiento> ordenes = ordenAbastecimientoRepository.findByTipoOrden(tipoOrden);
            System.out.println("Órdenes encontradas: " + ordenes.size());
            return ordenes;
        } catch (Exception e) {
            System.err.println("ERROR al buscar órdenes por tipo: " + e.getMessage());
            throw new RuntimeException("Error al buscar órdenes por tipo", e);
        }
    }

    public List<OrdenAbastecimiento> obtenerOrdenesPorEstado(OrdenAbastecimiento.EstadoOrden estado) {
        try {
            System.out.println("=== BUSCANDO ÓRDENES POR ESTADO: " + estado + " ===");
            List<OrdenAbastecimiento> ordenes = ordenAbastecimientoRepository.findByEstado(estado);
            System.out.println("Órdenes encontradas: " + ordenes.size());
            return ordenes;
        } catch (Exception e) {
            System.err.println("ERROR al buscar órdenes por estado: " + e.getMessage());
            throw new RuntimeException("Error al buscar órdenes por estado", e);
        }
    }

    private String generarNumeroOAUnico(OrdenAbastecimiento.TipoOrden tipoOrden) {
        String prefijo = obtenerPrefijoTipo(tipoOrden);
        String año = String.valueOf(Year.now().getValue());

        System.out.println("Generando número para tipo: " + tipoOrden + ", prefijo: " + prefijo + ", año: " + año);

        try {
            Long maxNumero = ordenAbastecimientoRepository.findMaxNumeroByPrefijoAndYear(prefijo, año);
            Long nextNumber = (maxNumero != null) ? maxNumero + 1 : 1;

            String numeroPropuesto = String.format("%s-%s-%03d", prefijo, año, nextNumber);
            System.out.println("Número propuesto: " + numeroPropuesto);

            if (!ordenAbastecimientoRepository.existsByNumeroOA(numeroPropuesto)) {
                return numeroPropuesto;
            }
        } catch (Exception e) {
            System.err.println("Error en generación automática: " + e.getMessage());
        }

        System.out.println("Buscando número disponible secuencialmente...");
        for (long i = 1; i <= 999; i++) {
            String numeroPropuesto = String.format("%s-%s-%03d", prefijo, año, i);
            if (!ordenAbastecimientoRepository.existsByNumeroOA(numeroPropuesto)) {
                System.out.println("Número generado (búsqueda secuencial): " + numeroPropuesto);
                return numeroPropuesto;
            }
        }

        String numeroFallback = generarNumeroFallback(prefijo, año);
        System.out.println("Número fallback: " + numeroFallback);
        return numeroFallback;
    }

    private String generarNumeroFallback(String prefijo, String año) {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(7, 12);
        return String.format("%s-%s-%s", prefijo, año, timestamp);
    }

    private String obtenerPrefijoTipo(OrdenAbastecimiento.TipoOrden tipoOrden) {
        return switch (tipoOrden) {
            case ALIMENTOS -> "ALM";
            case U_OFICINA -> "UOF";
            case R_UTILES -> "RUT";
            case OTROS -> "OTS";
            default -> "OA";
        };
    }

    private void calcularTotal(OrdenAbastecimiento ordenAbastecimiento) {
        if (ordenAbastecimiento.getItems() != null && !ordenAbastecimiento.getItems().isEmpty()) {
            double total = ordenAbastecimiento.getItems().stream()
                    .mapToDouble(item -> item.getSubtotal() != null ? item.getSubtotal().doubleValue() : 0.0)
                    .sum();
            ordenAbastecimiento.setTotal(BigDecimal.valueOf(total));
            System.out.println("Total calculado: S/ " + ordenAbastecimiento.getTotal());
        } else {
            ordenAbastecimiento.setTotal(BigDecimal.ZERO);
            System.out.println("Sin items, total: S/ 0.00");
        }
    }

    private void validarItemsOrden(OrdenAbastecimiento ordenAbastecimiento) {
        if (ordenAbastecimiento.getItems() == null || ordenAbastecimiento.getItems().isEmpty()) {
            System.out.println("Orden sin items - permitido para algunos tipos de orden");
            return;
        }

        List<OrdenAbastecimientoItem> itemsValidos = ordenAbastecimiento.getItems().stream()
                .filter(item -> item.getProducto() != null && item.getProducto().getId() != null)
                .collect(Collectors.toList());

        ordenAbastecimiento.setItems(itemsValidos);

        int itemsConDatosValidos = 0;
        for (OrdenAbastecimientoItem item : itemsValidos) {
            if (item.getCantidad() > 0 &&
                    item.getPrecioUnitario() != null &&
                    item.getPrecioUnitario().compareTo(BigDecimal.ZERO) >= 0) {
                itemsConDatosValidos++;
            }
        }

        System.out.println("Items válidos encontrados: " + itemsConDatosValidos + " de " + itemsValidos.size());

        if (itemsConDatosValidos == 0) {
            throw new IllegalArgumentException("La orden debe contener al menos un producto válido");
        }
    }

    public List<OrdenAbastecimiento> obtenerOrdenesPendientes() {
        try {
            System.out.println("=== BUSCANDO ÓRDENES PENDIENTES ===");
            List<OrdenAbastecimiento> ordenes = ordenAbastecimientoRepository.findOrdenesPendientes();
            System.out.println("Órdenes pendientes encontradas: " + ordenes.size());
            return ordenes;
        } catch (Exception e) {
            System.err.println("ERROR al buscar órdenes pendientes: " + e.getMessage());
            throw new RuntimeException("Error al buscar órdenes pendientes", e);
        }
    }

    public List<OrdenAbastecimiento> obtenerOrdenesDelMesActual() {
        try {
            System.out.println("=== BUSCANDO ÓRDENES DEL MES ACTUAL ===");
            List<OrdenAbastecimiento> ordenes = ordenAbastecimientoRepository.findOrdenesDelMesActual();
            System.out.println("Órdenes del mes actual: " + ordenes.size());
            return ordenes;
        } catch (Exception e) {
            System.err.println("ERROR al buscar órdenes del mes actual: " + e.getMessage());
            throw new RuntimeException("Error al buscar órdenes del mes actual", e);
        }
    }

    public boolean existeOrdenConNumero(String numeroOA) {
        try {
            return ordenAbastecimientoRepository.existsByNumeroOA(numeroOA);
        } catch (Exception e) {
            System.err.println("ERROR al verificar número de orden: " + e.getMessage());
            return false;
        }
    }

    public Object[] obtenerEstadisticasDashboard() {
        try {
            System.out.println("=== OBTENIENDO ESTADÍSTICAS PARA DASHBOARD ===");
            return ordenAbastecimientoRepository.findEstadisticasDashboard();
        } catch (Exception e) {
            System.err.println("ERROR al obtener estadísticas: " + e.getMessage());
            return new Object[]{0L, 0L, 0L, 0L, BigDecimal.ZERO}; // Valores por defecto
        }
    }
}