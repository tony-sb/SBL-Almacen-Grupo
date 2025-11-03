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

@Service
public class OrdenAbastecimientoService { // CAMBIADO: OrdenCompraService → OrdenAbastecimientoService

    @Autowired
    private OrdenAbastecimientoRepository ordenAbastecimientoRepository; // CAMBIADO

    @Autowired
    private ProveedorRepository proveedorRepository;

    /**
     * Obtiene todas las órdenes con relaciones cargadas
     */
    public List<OrdenAbastecimiento> obtenerTodasOrdenes() { // CAMBIADO: retorno
        try {
            System.out.println("=== BUSCANDO TODAS LAS ÓRDENES DE ABASTECIMIENTO ===");
            List<OrdenAbastecimiento> ordenes = ordenAbastecimientoRepository.findAllWithProveedorAndUsuario(); // CAMBIADO
            System.out.println("Órdenes encontradas: " + ordenes.size());

            // Log detallado para debugging
            for (OrdenAbastecimiento orden : ordenes) {
                System.out.println(" []  " + orden.getNumeroOA() + // CAMBIADO
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
                List<OrdenAbastecimiento> ordenes = ordenAbastecimientoRepository.findAll(); // CAMBIADO
                System.out.println("Órdenes con método simple: " + ordenes.size());
                return ordenes;
            } catch (Exception ex) {
                System.err.println("ERROR crítico: " + ex.getMessage());
                throw new RuntimeException("Error al cargar órdenes de abastecimiento", ex); // CAMBIADO
            }
        }
    }

    /**
     * Obtiene una orden por ID con todos sus items
     */
    public Optional<OrdenAbastecimiento> obtenerOrdenPorId(Long id) { // CAMBIADO: retorno
        try {
            System.out.println("=== BUSCANDO ORDEN POR ID: " + id + " ===");
            Optional<OrdenAbastecimiento> orden = ordenAbastecimientoRepository.findByIdWithItems(id); // CAMBIADO

            if (orden.isPresent()) {
                OrdenAbastecimiento ordenEncontrada = orden.get();

                // VALIDACIÓN CRÍTICA: Filtrar items con productos nulos
                if (ordenEncontrada.getItems() != null) {
                    List<OrdenAbastecimientoItem> itemsValidos = ordenEncontrada.getItems().stream() // CAMBIADO
                            .filter(item -> item.getProducto() != null)
                            .collect(Collectors.toList());

                    ordenEncontrada.setItems(itemsValidos);
                }

                System.out.println("Orden encontrada: " + ordenEncontrada.getNumeroOA()); // CAMBIADO
                System.out.println("Items válidos en orden: " +
                        (ordenEncontrada.getItems() != null ? ordenEncontrada.getItems().size() : 0));

                // Log de items para debugging
                if (ordenEncontrada.getItems() != null) {
                    for (OrdenAbastecimientoItem item : ordenEncontrada.getItems()) { // CAMBIADO
                        System.out.println(" ° " + item.getProducto().getNombre() +
                                " x " + item.getCantidad() +
                                " = S/ " + item.getSubtotal());
                    }
                }
            } else {
                System.out.println("Orden no encontrada con ID: " + id);
            }

            return orden;
        } catch (Exception e) {
            System.err.println("ERROR cargando orden con items: " + e.getMessage());
            e.printStackTrace();
            return ordenAbastecimientoRepository.findById(id); // CAMBIADO
        }
    }

    /**
     * Guarda o actualiza una orden de abastecimiento
     */
    @Transactional
    public OrdenAbastecimiento guardarOrden(OrdenAbastecimiento ordenAbastecimiento) { // CAMBIADO: parámetro
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
            if (ordenAbastecimiento.getNumeroOA() == null || ordenAbastecimiento.getNumeroOA().isEmpty()) { // CAMBIADO
                String nuevoNumero = generarNumeroOAUnico(ordenAbastecimiento.getTipoOrden()); // CAMBIADO
                ordenAbastecimiento.setNumeroOA(nuevoNumero); // CAMBIADO
                System.out.println("Número de orden generado: " + nuevoNumero);
            } else {
                // Verificar que el número proporcionado no esté duplicado (solo para nuevas órdenes)
                if (ordenAbastecimiento.getId() == null) {
                    boolean existe = ordenAbastecimientoRepository.existsByNumeroOA(ordenAbastecimiento.getNumeroOA()); // CAMBIADO
                    if (existe) {
                        throw new RuntimeException("El número de orden ya existe: " + ordenAbastecimiento.getNumeroOA()); // CAMBIADO
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

            System.out.println("Guardando orden con número: " + ordenAbastecimiento.getNumeroOA()); // CAMBIADO
            OrdenAbastecimiento ordenGuardada = ordenAbastecimientoRepository.save(ordenAbastecimiento); // CAMBIADO

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
     * Elimina una orden de abastecimiento
     */
    @Transactional
    public void eliminarOrden(Long id) {
        try {
            System.out.println("=== ELIMINANDO ORDEN - ID: " + id + " ===");

            // Verificar que la orden existe
            if (!ordenAbastecimientoRepository.existsById(id)) { // CAMBIADO
                throw new IllegalArgumentException("Orden no encontrada con ID: " + id);
            }

            ordenAbastecimientoRepository.deleteById(id); // CAMBIADO
            System.out.println("Orden eliminada correctamente");

        } catch (Exception e) {
            System.err.println("ERROR al eliminar orden: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar la orden: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene órdenes por tipo
     */
    public List<OrdenAbastecimiento> obtenerOrdenesPorTipo(OrdenAbastecimiento.TipoOrden tipoOrden) { // CAMBIADO: retorno
        try {
            System.out.println("=== BUSCANDO ÓRDENES POR TIPO: " + tipoOrden + " ===");
            List<OrdenAbastecimiento> ordenes = ordenAbastecimientoRepository.findByTipoOrden(tipoOrden); // CAMBIADO
            System.out.println("Órdenes encontradas: " + ordenes.size());
            return ordenes;
        } catch (Exception e) {
            System.err.println("ERROR al buscar órdenes por tipo: " + e.getMessage());
            throw new RuntimeException("Error al buscar órdenes por tipo", e);
        }
    }

    /**
     * Obtiene órdenes por estado
     */
    public List<OrdenAbastecimiento> obtenerOrdenesPorEstado(OrdenAbastecimiento.EstadoOrden estado) { // CAMBIADO: retorno
        try {
            System.out.println("=== BUSCANDO ÓRDENES POR ESTADO: " + estado + " ===");
            List<OrdenAbastecimiento> ordenes = ordenAbastecimientoRepository.findByEstado(estado); // CAMBIADO
            System.out.println("Órdenes encontradas: " + ordenes.size());
            return ordenes;
        } catch (Exception e) {
            System.err.println("ERROR al buscar órdenes por estado: " + e.getMessage());
            throw new RuntimeException("Error al buscar órdenes por estado", e);
        }
    }

    // ================= MÉTODOS PRIVADOS AUXILIARES =================

    /**
     * Genera un número de orden único evitando duplicados
     */
    private String generarNumeroOAUnico(OrdenAbastecimiento.TipoOrden tipoOrden) { // CAMBIADO: nombre
        String prefijo = obtenerPrefijoTipo(tipoOrden);
        String año = String.valueOf(Year.now().getValue());

        System.out.println("Generando número para tipo: " + tipoOrden + ", prefijo: " + prefijo + ", año: " + año);

        try {
            // Método 1: Buscar el máximo número existente
            Long maxNumero = ordenAbastecimientoRepository.findMaxNumeroByPrefijoAndYear(prefijo, año); // CAMBIADO
            Long nextNumber = (maxNumero != null) ? maxNumero + 1 : 1;

            String numeroPropuesto = String.format("%s-%s-%03d", prefijo, año, nextNumber);
            System.out.println("Número propuesto: " + numeroPropuesto);

            // Verificar que no exista
            if (!ordenAbastecimientoRepository.existsByNumeroOA(numeroPropuesto)) { // CAMBIADO
                return numeroPropuesto;
            }
        } catch (Exception e) {
            System.err.println("Error en generación automática: " + e.getMessage());
        }

        // Método 2: Búsqueda secuencial (más seguro)
        System.out.println("Buscando número disponible secuencialmente...");
        for (long i = 1; i <= 999; i++) {
            String numeroPropuesto = String.format("%s-%s-%03d", prefijo, año, i);
            if (!ordenAbastecimientoRepository.existsByNumeroOA(numeroPropuesto)) { // CAMBIADO
                System.out.println("úmero generado (búsqueda secuencial): " + numeroPropuesto);
                return numeroPropuesto;
            }
        }

        // Método 3: Usar timestamp como fallback
        String numeroFallback = generarNumeroFallback(prefijo, año);
        System.out.println("Número fallback: " + numeroFallback);
        return numeroFallback;
    }

    /**
     * Genera número de fallback usando timestamp
     */
    private String generarNumeroFallback(String prefijo, String año) {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(7, 12);
        return String.format("%s-%s-%s", prefijo, año, timestamp);
    }

    /**
     * Obtiene el prefijo según el tipo de orden
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
            default -> "OA"; // CAMBIADO: OC → OA
        };
    }

    /**
     * Calcula el total de la orden basado en los items
     */
    private void calcularTotal(OrdenAbastecimiento ordenAbastecimiento) { // CAMBIADO: parámetro
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
     * Valida los items de la orden antes de guardar
     */
    private void validarItemsOrden(OrdenAbastecimiento ordenAbastecimiento) { // CAMBIADO: parámetro
        if (ordenAbastecimiento.getItems() == null || ordenAbastecimiento.getItems().isEmpty()) {
            System.out.println("Orden sin items - permitido para algunos tipos de orden");
            return;
        }

        // Filtrar items con productos nulos
        List<OrdenAbastecimientoItem> itemsValidos = ordenAbastecimiento.getItems().stream() // CAMBIADO
                .filter(item -> item.getProducto() != null && item.getProducto().getId() != null)
                .collect(Collectors.toList());

        ordenAbastecimiento.setItems(itemsValidos);

        int itemsConDatosValidos = 0;
        for (OrdenAbastecimientoItem item : itemsValidos) { // CAMBIADO
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

    /**
     * Método adicional: Buscar órdenes por rango de fechas
     */
    public List<OrdenAbastecimiento> obtenerOrdenesPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) { // CAMBIADO: retorno
        try {
            System.out.println("=== BUSCANDO ÓRDENES POR RANGO: " + fechaInicio + " a " + fechaFin + " ===");
            // Implementar según necesidad en el repository
            return ordenAbastecimientoRepository.findAll(); // CAMBIADO
        } catch (Exception e) {
            System.err.println("ERROR al buscar órdenes por rango: " + e.getMessage());
            throw new RuntimeException("Error al buscar órdenes por rango de fechas", e);
        }
    }

    // ================= MÉTODOS ADICIONALES =================

    /**
     * Obtiene órdenes pendientes
     */
    public List<OrdenAbastecimiento> obtenerOrdenesPendientes() { // CAMBIADO: retorno
        try {
            System.out.println("=== BUSCANDO ÓRDENES PENDIENTES ===");
            List<OrdenAbastecimiento> ordenes = ordenAbastecimientoRepository.findOrdenesPendientes(); // CAMBIADO
            System.out.println("Órdenes pendientes encontradas: " + ordenes.size());
            return ordenes;
        } catch (Exception e) {
            System.err.println("ERROR al buscar órdenes pendientes: " + e.getMessage());
            throw new RuntimeException("Error al buscar órdenes pendientes", e);
        }
    }

    /**
     * Obtiene órdenes del mes actual
     */
    public List<OrdenAbastecimiento> obtenerOrdenesDelMesActual() { // CAMBIADO: retorno
        try {
            System.out.println("=== BUSCANDO ÓRDENES DEL MES ACTUAL ===");
            List<OrdenAbastecimiento> ordenes = ordenAbastecimientoRepository.findOrdenesDelMesActual(); // CAMBIADO
            System.out.println("Órdenes del mes actual: " + ordenes.size());
            return ordenes;
        } catch (Exception e) {
            System.err.println("ERROR al buscar órdenes del mes actual: " + e.getMessage());
            throw new RuntimeException("Error al buscar órdenes del mes actual", e);
        }
    }

    /**
     * Verifica si existe una orden con el número dado
     */
    public boolean existeOrdenConNumero(String numeroOA) { // CAMBIADO: parámetro
        try {
            return ordenAbastecimientoRepository.existsByNumeroOA(numeroOA); // CAMBIADO
        } catch (Exception e) {
            System.err.println("ERROR al verificar número de orden: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene estadísticas para dashboard
     */
    public Object[] obtenerEstadisticasDashboard() {
        try {
            System.out.println("=== OBTENIENDO ESTADÍSTICAS PARA DASHBOARD ===");
            return ordenAbastecimientoRepository.findEstadisticasDashboard(); // CAMBIADO
        } catch (Exception e) {
            System.err.println("ERROR al obtener estadísticas: " + e.getMessage());
            return new Object[]{0L, 0L, 0L, 0L, BigDecimal.ZERO}; // Valores por defecto
        }
    }
}