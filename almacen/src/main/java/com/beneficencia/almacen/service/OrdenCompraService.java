package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.*;
import com.beneficencia.almacen.repository.OrdenCompraRepository;
import com.beneficencia.almacen.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
public class OrdenCompraService {

    @Autowired
    private OrdenCompraRepository ordenCompraRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    public List<OrdenCompra> obtenerTodasOrdenes() {
        try {
            System.out.println("=== INTENTANDO CARGAR ÓRDENES ===");
            List<OrdenCompra> ordenes = ordenCompraRepository.findAllWithProveedorAndUsuario();
            System.out.println("Órdenes encontradas: " + ordenes.size());

            // Verificar que las relaciones estén cargadas
            for (OrdenCompra orden : ordenes) {
                if (orden.getProveedor() == null) {
                    System.out.println("⚠️ Orden " + orden.getId() + " tiene proveedor null");
                }
                if (orden.getUsuario() == null) {
                    System.out.println("⚠️ Orden " + orden.getId() + " tiene usuario null");
                }
            }

            return ordenes;
        } catch (Exception e) {
            System.err.println("ERROR en consulta: " + e.getMessage());
            System.out.println("=== USANDO MÉTODO SIMPLE ===");

            // Método simple de respaldo
            List<OrdenCompra> ordenes = ordenCompraRepository.findAll();
            System.out.println("Órdenes con método simple: " + ordenes.size());
            return ordenes;
        }
    }
    public Optional<OrdenCompra> obtenerOrdenPorId(Long id) {
        try {
            return ordenCompraRepository.findByIdWithItems(id);
        } catch (Exception e) {
            System.err.println("Error cargando orden con items: " + e.getMessage());
            // Método simple
            return ordenCompraRepository.findById(id);
        }
    }

    @Transactional
    public OrdenCompra guardarOrden(OrdenCompra ordenCompra) {
        try {
            System.out.println("=== INICIANDO GUARDADO DE ORDEN ===");

            // Generar número de OC automáticamente si no existe
            if (ordenCompra.getNumeroOC() == null || ordenCompra.getNumeroOC().isEmpty()) {
                String nuevoNumero = generarNumeroOCUnico(ordenCompra.getTipoOrden());
                ordenCompra.setNumeroOC(nuevoNumero);
                System.out.println("Número de orden generado: " + nuevoNumero);
            } else {
                // Verificar que el número proporcionado no esté duplicado
                if (ordenCompra.getId() == null) { // Solo para nuevas órdenes
                    boolean existe = ordenCompraRepository.existsByNumeroOC(ordenCompra.getNumeroOC());
                    if (existe) {
                        throw new RuntimeException("El número de orden ya existe: " + ordenCompra.getNumeroOC());
                    }
                }
            }

            // Calcular total
            calcularTotal(ordenCompra);

            // Establecer fechas si no existen
            if (ordenCompra.getFechaCreacion() == null) {
                ordenCompra.setFechaCreacion(LocalDateTime.now());
            }
            ordenCompra.setFechaActualizacion(LocalDateTime.now());

            // Establecer estado por defecto si no existe
            if (ordenCompra.getEstado() == null) {
                ordenCompra.setEstado(OrdenCompra.EstadoOrden.PENDIENTE);
            }

            System.out.println("Guardando orden con número: " + ordenCompra.getNumeroOC());
            OrdenCompra ordenGuardada = ordenCompraRepository.save(ordenCompra);
            System.out.println("Orden guardada exitosamente con ID: " + ordenGuardada.getId());

            return ordenGuardada;

        } catch (Exception e) {
            System.err.println("ERROR al guardar orden: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-lanzar la excepción para que el controller la capture
        }
    }

    public void eliminarOrden(Long id) {
        ordenCompraRepository.deleteById(id);
    }

    public List<OrdenCompra> obtenerOrdenesPorTipo(OrdenCompra.TipoOrden tipoOrden) {
        return ordenCompraRepository.findByTipoOrden(tipoOrden);
    }

    /**
     * Genera un número de orden único evitando duplicados
     */
    private String generarNumeroOCUnico(OrdenCompra.TipoOrden tipoOrden) {
        String prefijo = obtenerPrefijoTipo(tipoOrden);
        String año = String.valueOf(Year.now().getValue());

        System.out.println("Generando número para tipo: " + tipoOrden + ", prefijo: " + prefijo + ", año: " + año);

        // Método 1: Buscar el máximo número existente
        try {
            Long maxNumero = ordenCompraRepository.findMaxNumeroByPrefijoAndYear(prefijo, año);
            Long nextNumber = (maxNumero != null) ? maxNumero + 1 : 1;

            String numeroPropuesto = String.format("%s-%s-%03d", prefijo, año, nextNumber);
            System.out.println("Número propuesto (método 1): " + numeroPropuesto);

            // Verificar que no exista
            if (!ordenCompraRepository.existsByNumeroOC(numeroPropuesto)) {
                return numeroPropuesto;
            }
        } catch (Exception e) {
            System.err.println("Error en método 1, usando método 2: " + e.getMessage());
        }

        // Método 2: Búsqueda secuencial (más seguro)
        for (long i = 1; i <= 999; i++) {
            String numeroPropuesto = String.format("%s-%s-%03d", prefijo, año, i);
            if (!ordenCompraRepository.existsByNumeroOC(numeroPropuesto)) {
                System.out.println("Número generado (método 2): " + numeroPropuesto);
                return numeroPropuesto;
            }
        }

        // Método 3: Usar timestamp como fallback
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(9);
        String numeroFallback = String.format("%s-%s-%s", prefijo, año, timestamp);
        System.out.println("Número fallback (método 3): " + numeroFallback);

        return numeroFallback;
    }

    /**
     * Método original mejorado con verificación de duplicados
     */
    private String generarNumeroOC(OrdenCompra.TipoOrden tipoOrden) {
        String prefijo = obtenerPrefijoTipo(tipoOrden);
        String año = String.valueOf(Year.now().getValue());

        // Contar órdenes existentes de este tipo y año
        Long count = ordenCompraRepository.countByTipoOrdenAndFechaOCYear(tipoOrden, Year.now().getValue());
        Long nextNumber = count + 1;

        String numeroPropuesto = String.format("%s-%s-%03d", prefijo, año, nextNumber);

        // Verificar que no exista
        int intentos = 0;
        while (ordenCompraRepository.existsByNumeroOC(numeroPropuesto) && intentos < 10) {
            nextNumber++;
            numeroPropuesto = String.format("%s-%s-%03d", prefijo, año, nextNumber);
            intentos++;
        }

        if (intentos >= 10) {
            // Fallback: usar timestamp
            String timestamp = String.valueOf(System.currentTimeMillis()).substring(9);
            numeroPropuesto = String.format("%s-%s-%s", prefijo, año, timestamp);
        }

        return numeroPropuesto;
    }

    private String obtenerPrefijoTipo(OrdenCompra.TipoOrden tipoOrden) {
        return switch (tipoOrden) {
            case SOLIDAS -> "SOL";
            case DONACIONES -> "DON";
            case U_OFICINA -> "UOF";
            case INVENTARIO -> "INV";
            case REPORTE -> "REP";
            case R_DONACION -> "RDON";
            case R_UTILES -> "RUT";
            case R_TOTAL -> "RTOT";
            default -> "OC";
        };
    }

    private void calcularTotal(OrdenCompra ordenCompra) {
        if (ordenCompra.getItems() != null && !ordenCompra.getItems().isEmpty()) {
            double total = ordenCompra.getItems().stream()
                    .mapToDouble(item -> item.getSubtotal().doubleValue())
                    .sum();
            ordenCompra.setTotal(java.math.BigDecimal.valueOf(total));
            System.out.println("Total calculado: " + ordenCompra.getTotal());
        } else {
            ordenCompra.setTotal(java.math.BigDecimal.ZERO);
            System.out.println("Sin items, total: 0");
        }
    }
}