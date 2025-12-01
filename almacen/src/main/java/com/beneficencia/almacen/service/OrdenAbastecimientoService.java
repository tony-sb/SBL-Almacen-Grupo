package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.*;
import com.beneficencia.almacen.repository.OrdenAbastecimientoRepository;
import com.beneficencia.almacen.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de órdenes de abastecimiento del almacén.
 * Maneja las operaciones CRUD de órdenes de abastecimiento, incluyendo
 * generación automática de números, validaciones, cálculos de totales
 * y procesamiento de items.
 */
@Service
public class OrdenAbastecimientoService {

    @Autowired
    private OrdenAbastecimientoRepository ordenAbastecimientoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    /**
     * Obtiene todas las órdenes de abastecimiento con relaciones cargadas.
     * Incluye información de proveedor y usuario para visualización completa.
     *
     * @return Lista de todas las órdenes de abastecimiento ordenadas por fecha de creación descendente
     * @throws RuntimeException si ocurre un error al cargar las órdenes
     */
    public List<OrdenAbastecimiento> obtenerTodasOrdenes() {
        try {
            System.out.println("=== BUSCANDO TODAS LAS ÓRDENES DE ABASTECIMIENTO ===");
            List<OrdenAbastecimiento> ordenes = ordenAbastecimientoRepository.findAllWithProveedorAndUsuario();
            System.out.println("Órdenes encontradas: " + ordenes.size());

            // Log detallado para debugging
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
            // Método simple de respaldo
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

    /**
     * Obtiene una orden de abastecimiento por ID con todos sus items cargados.
     * Filtra automáticamente items con productos nulos para evitar errores.
     *
     * @param id ID de la orden a buscar
     * @return Optional con la orden encontrada y sus items válidos, o vacío si no existe
     */
    public Optional<OrdenAbastecimiento> obtenerOrdenPorId(Long id) {
        try {
            System.out.println("=== BUSCANDO ORDEN POR ID: " + id + " ===");
            Optional<OrdenAbastecimiento> orden = ordenAbastecimientoRepository.findByIdWithItems(id);

            if (orden.isPresent()) {
                OrdenAbastecimiento ordenEncontrada = orden.get();

                // CORRECCIÓN CRÍTICA: Filtrar items con productos nulos
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

    /**
     * Guarda o actualiza una orden de abastecimiento.
     * Realiza validaciones, genera número automático, calcula totales y procesa items.
     *
     * @param ordenAbastecimiento Orden de abastecimiento a guardar o actualizar
     * @return Orden de abastecimiento guardada con ID y datos actualizados
     * @throws RuntimeException si hay errores de validación o persistencia
     */
    @Transactional
    public OrdenAbastecimiento guardarOrden(OrdenAbastecimiento ordenAbastecimiento) {
        try {
            System.out.println("=== INICIANDO GUARDADO DE ORDEN DE ABASTECIMIENTO ===");

            // Validaciones básicas
            if (ordenAbastecimiento.getTipoOrden() == null) {
                throw new IllegalArgumentException("El tipo de orden es requerido");
            }
            if (ordenAbastecimiento.getProveedor() == null) {
                throw new IllegalArgumentException("El proveedor es requerido");
            }

            // Generar número de OA automáticamente si no existe
            if (ordenAbastecimiento.getNumeroOA() == null || ordenAbastecimiento.getNumeroOA().isEmpty()) {
                String nuevoNumero = generarNumeroOAUnico(ordenAbastecimiento.getTipoOrden());
                ordenAbastecimiento.setNumeroOA(nuevoNumero);
                System.out.println("Número de orden generado: " + nuevoNumero);
            } else {
                // Verificar que el número proporcionado no esté duplicado (solo para nuevas órdenes)
                if (ordenAbastecimiento.getId() == null) {
                    boolean existe = ordenAbastecimientoRepository.existsByNumeroOA(ordenAbastecimiento.getNumeroOA());
                    if (existe) {
                        throw new RuntimeException("El número de orden ya existe: " + ordenAbastecimiento.getNumeroOA());
                    }
                }
            }

            // Calcular total basado en los items
            calcularTotal(ordenAbastecimiento);

            // Establecer fechas si no existen
            if (ordenAbastecimiento.getFechaCreacion() == null) {
                ordenAbastecimiento.setFechaCreacion(LocalDateTime.now());
                System.out.println("Fecha de creación establecida");
            }
            ordenAbastecimiento.setFechaActualizacion(LocalDateTime.now());

            // Establecer estado por defecto si no existe
            if (ordenAbastecimiento.getEstado() == null) {
                ordenAbastecimiento.setEstado(OrdenAbastecimiento.EstadoOrden.PENDIENTE);
                System.out.println("Estado establecido: PENDIENTE");
            }

            // Validar items antes de guardar
            validarItemsOrden(ordenAbastecimiento);

            System.out.println("Guardando orden con número: " + ordenAbastecimiento.getNumeroOA());
            OrdenAbastecimiento ordenGuardada = ordenAbastecimientoRepository.save(ordenAbastecimiento);

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

    /**
     * Elimina una orden de abastecimiento por ID.
     * Realiza validación de existencia antes de proceder con la eliminación.
     *
     * @param id ID de la orden a eliminar
     * @throws RuntimeException si la orden no existe o hay errores durante la eliminación
     */
    @Transactional
    public void eliminarOrden(Long id) {
        try {
            System.out.println("=== ELIMINANDO ORDEN - ID: " + id + " ===");

            // Verificar que la orden existe
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

    /**
     * Obtiene órdenes de abastecimiento por tipo específico.
     *
     * @param tipoOrden Tipo de orden a filtrar
     * @return Lista de órdenes que coinciden con el tipo especificado
     * @throws RuntimeException si ocurre un error en la consulta
     */
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

    /**
     * Obtiene órdenes de abastecimiento por estado específico.
     *
     * @param estado Estado de la orden a filtrar
     * @return Lista de órdenes que coinciden con el estado especificado
     * @throws RuntimeException si ocurre un error en la consulta
     */
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

    // ================= MÉTODOS PRIVADOS AUXILIARES =================

    /**
     * Genera un número de orden único basado en tipo y año, evitando duplicados.
     * Utiliza múltiples estrategias para garantizar la unicidad.
     *
     * @param tipoOrden Tipo de orden para determinar el prefijo
     * @return Número de orden único generado
     */
    private String generarNumeroOAUnico(OrdenAbastecimiento.TipoOrden tipoOrden) {
        String prefijo = obtenerPrefijoTipo(tipoOrden);
        String año = String.valueOf(Year.now().getValue());

        System.out.println("Generando número para tipo: " + tipoOrden + ", prefijo: " + prefijo + ", año: " + año);

        try {
            // Método 1: Buscar el máximo número existente
            Long maxNumero = ordenAbastecimientoRepository.findMaxNumeroByPrefijoAndYear(prefijo, año);
            Long nextNumber = (maxNumero != null) ? maxNumero + 1 : 1;

            String numeroPropuesto = String.format("%s-%s-%03d", prefijo, año, nextNumber);
            System.out.println("Número propuesto: " + numeroPropuesto);

            // Verificar que no exista
            if (!ordenAbastecimientoRepository.existsByNumeroOA(numeroPropuesto)) {
                return numeroPropuesto;
            }
        } catch (Exception e) {
            System.err.println("Error en generación automática: " + e.getMessage());
        }

        // Método 2: Búsqueda secuencial (más seguro)
        System.out.println("Buscando número disponible secuencialmente...");
        for (long i = 1; i <= 999; i++) {
            String numeroPropuesto = String.format("%s-%s-%03d", prefijo, año, i);
            if (!ordenAbastecimientoRepository.existsByNumeroOA(numeroPropuesto)) {
                System.out.println("Número generado (búsqueda secuencial): " + numeroPropuesto);
                return numeroPropuesto;
            }
        }

        // Método 3: Usar timestamp como fallback
        String numeroFallback = generarNumeroFallback(prefijo, año);
        System.out.println("Número fallback: " + numeroFallback);
        return numeroFallback;
    }

    /**
     * Genera número de fallback usando timestamp cuando no hay números disponibles.
     *
     * @param prefijo Prefijo del tipo de orden
     * @param año Año actual
     * @return Número de orden único basado en timestamp
     */
    private String generarNumeroFallback(String prefijo, String año) {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(7, 12);
        return String.format("%s-%s-%s", prefijo, año, timestamp);
    }

    /**
     * Obtiene el prefijo correspondiente al tipo de orden.
     *
     * @param tipoOrden Tipo de orden
     * @return Prefijo de 2-4 caracteres para el número de orden
     */
    private String obtenerPrefijoTipo(OrdenAbastecimiento.TipoOrden tipoOrden) {
        return switch (tipoOrden) {
            case SOLIDAS -> "SOL";
            case DONACIONES -> "DON";
            case U_OFICINA -> "UOF";
            case INVENTARIO -> "INV";
            case REPORTE -> "REP";
            case R_DONACION -> "RDON";
            case R_UTILES -> "RUT";
            case R_TOTAL -> "RTOT";
            default -> "OA";
        };
    }

    /**
     * Calcula el total de la orden basado en los subtotales de los items.
     *
     * @param ordenAbastecimiento Orden a la que se calculará el total
     */
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

    /**
     * Valida los items de la orden antes de guardar.
     * Filtra items inválidos y verifica que haya al menos un producto válido.
     *
     * @param ordenAbastecimiento Orden cuyos items serán validados
     * @throws IllegalArgumentException si no hay items válidos en la orden
     */
    private void validarItemsOrden(OrdenAbastecimiento ordenAbastecimiento) {
        if (ordenAbastecimiento.getItems() == null || ordenAbastecimiento.getItems().isEmpty()) {
            System.out.println("Orden sin items - permitido para algunos tipos de orden");
            return;
        }

        // Filtrar items con productos nulos
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

    // ================= MÉTODOS ADICIONALES =================

    /**
     * Obtiene las órdenes de abastecimiento pendientes.
     * Ordenadas por fecha de orden ascendente para priorizar las más antiguas.
     *
     * @return Lista de órdenes con estado PENDIENTE
     * @throws RuntimeException si ocurre un error en la consulta
     */
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

    /**
     * Obtiene las órdenes de abastecimiento del mes actual.
     * Útil para reportes mensuales y dashboards.
     *
     * @return Lista de órdenes del mes actual ordenadas por fecha descendente
     * @throws RuntimeException si ocurre un error en la consulta
     */
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

    /**
     * Verifica si existe una orden con el número dado.
     *
     * @param numeroOA Número de orden a verificar
     * @return true si existe una orden con ese número, false en caso contrario
     */
    public boolean existeOrdenConNumero(String numeroOA) {
        try {
            return ordenAbastecimientoRepository.existsByNumeroOA(numeroOA);
        } catch (Exception e) {
            System.err.println("ERROR al verificar número de orden: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene estadísticas consolidadas para el dashboard.
     * Incluye total de órdenes, órdenes por estado y monto total del año actual.
     *
     * @return Array de objetos con las estadísticas:
     *         [0] = totalOrdenes (Long)
     *         [1] = pendientes (Long)
     *         [2] = aprobadas (Long)
     *         [3] = completadas (Long)
     *         [4] = totalMonto (BigDecimal)
     */
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