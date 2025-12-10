package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.*;
import com.beneficencia.almacen.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrdenSalidaService {

    @Autowired
    private OrdenSalidaRepository ordenSalidaRepository;

    @Autowired
    private OrdenSalidaItemRepository ordenSalidaItemRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    /**
     * Obtiene todas las órdenes de salida ordenadas por fecha.
     */
    public List<OrdenSalida> obtenerTodasOrdenes() {
        return ordenSalidaRepository.findAllOrderByFecha();
    }

    /**
     * Obtiene una orden de salida por su ID con sus items.
     */
    public Optional<OrdenSalida> obtenerOrdenPorId(Long id) {
        return ordenSalidaRepository.findById(id);
    }

    /**
     * Obtiene una orden con todos sus items cargados.
     */
    public Optional<OrdenSalida> obtenerOrdenConItems(Long id) {
        Optional<OrdenSalida> ordenOpt = ordenSalidaRepository.findById(id);
        if (ordenOpt.isPresent()) {
            OrdenSalida orden = ordenOpt.get();
            List<OrdenSalidaItem> items = ordenSalidaItemRepository.findItemsConProductosPorOrdenId(id);
            orden.setItems(items);
        }
        return ordenOpt;
    }

    /**
     * Busca órdenes de salida dentro de un rango de fechas.
     */
    public List<OrdenSalida> buscarPorFecha(LocalDate fechaInicio, LocalDate fechaFin) {
        return ordenSalidaRepository.findByFechaSalidaBetween(fechaInicio, fechaFin);
    }

    /**
     * Busca órdenes de salida por DNI del usuario.
     */
    public List<OrdenSalida> buscarPorDniUsuario(String dni) {
        return ordenSalidaRepository.findByDniUsuarioContaining(dni);
    }

    /**
     * Busca órdenes de salida por número de trámite.
     */
    public List<OrdenSalida> buscarPorNumeroTramite(String tramite) {
        return ordenSalidaRepository.findByNumeroTramiteContaining(tramite);
    }

    /**
     * Busca una orden de salida por su número de orden único.
     */
    public Optional<OrdenSalida> buscarPorNumeroOrden(String numeroOrden) {
        return ordenSalidaRepository.findByNumeroOrden(numeroOrden);
    }

    /**
     * Guarda una orden de salida con sus items y actualiza el inventario.
     */
    @Transactional
    public OrdenSalida guardarOrdenConItems(OrdenSalida ordenSalida, List<OrdenSalidaItem> items) {
        // 1. Obtener usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

        ordenSalida.setUsuario(usuario);

        // 2. Buscar o crear beneficiario
        Beneficiario beneficiario = null;
        if (ordenSalida.getDniUsuario() != null && ordenSalida.getNombreUsuario() != null) {
            beneficiario = beneficiarioRepository.findByDni(ordenSalida.getDniUsuario())
                    .orElseGet(() -> {
                        Beneficiario nuevoBeneficiario = new Beneficiario();
                        nuevoBeneficiario.setDni(ordenSalida.getDniUsuario());

                        // Separar nombres y apellidos
                        String[] nombres = ordenSalida.getNombreUsuario().split(" ", 2);
                        if (nombres.length > 0) {
                            nuevoBeneficiario.setNombres(nombres[0]);
                        }
                        if (nombres.length > 1) {
                            nuevoBeneficiario.setApellidos(nombres[1]);
                        }

                        return beneficiarioRepository.save(nuevoBeneficiario);
                    });
        }
        ordenSalida.setBeneficiario(beneficiario);

        // 3. Generar números de orden si no existen
        if (ordenSalida.getNumeroOrden() == null || ordenSalida.getNumeroOrden().isEmpty()) {
            ordenSalida.setNumeroOrden(generarNumeroOrden());
        }
        if (ordenSalida.getNumeroOrdenSalida() == null || ordenSalida.getNumeroOrdenSalida().isEmpty()) {
            ordenSalida.setNumeroOrdenSalida(generarNumeroOrdenSalida(ordenSalida.getFechaSalida()));
        }

        // 4. Establecer fechas
        if (ordenSalida.getFechaRegistro() == null) {
            ordenSalida.setFechaRegistro(LocalDateTime.now());
        }
        ordenSalida.setFechaActualizacion(LocalDateTime.now());

        // 5. Guardar la orden primero
        OrdenSalida ordenGuardada = ordenSalidaRepository.save(ordenSalida);

        // 6. Procesar items

        // 6. Procesar items
        if (items != null && !items.isEmpty()) {
            for (OrdenSalidaItem item : items) {
                // Validar y actualizar stock
                Producto producto = productoRepository.findById(item.getProducto().getId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProducto().getId()));

                if (producto.getCantidad() < item.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para " + producto.getNombre() +
                            ". Disponible: " + producto.getCantidad() + ", Solicitado: " + item.getCantidad());
                }

                // Actualizar stock
                producto.setCantidad(producto.getCantidad() - item.getCantidad());
                productoRepository.save(producto);

                // Establecer relación y precio
                item.setOrdenSalida(ordenGuardada);
                if (item.getPrecioUnitario() == null || item.getPrecioUnitario().compareTo(BigDecimal.ZERO) == 0) {
                    item.setPrecioUnitario(producto.getPrecioUnitario());
                }

                // Guardar item
                ordenSalidaItemRepository.save(item);

                // ⭐⭐ AQUÍ VA EL CÓDIGO PARA REGISTRAR EL MOVIMIENTO ⭐⭐
                // Crear y guardar movimiento de inventario
                MovimientoInventario movimiento = crearMovimientoSalida(
                        producto,
                        item.getCantidad(),
                        "Orden de salida: " + ordenGuardada.getNumeroOrden(),
                        usuario,
                        ordenGuardada
                );
                movimientoInventarioRepository.save(movimiento);
            }

            // Actualizar cantidad total de productos en la orden
            ordenGuardada.setCantidadProductos(
                    items.stream().mapToInt(OrdenSalidaItem::getCantidad).sum()
            );
        }

        return ordenSalidaRepository.save(ordenGuardada);
    }

    // ⭐⭐ Método helper para crear movimientos ⭐⭐
    private MovimientoInventario crearMovimientoSalida(Producto producto, Integer cantidad,
                                                       String motivo, Usuario usuario,
                                                       OrdenSalida ordenSalida) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setTipoMovimiento(MovimientoInventario.TipoMovimiento.SALIDA);
        movimiento.setCantidad(cantidad);
        movimiento.setMotivo(motivo);
        movimiento.setUsuario(usuario);
        movimiento.setOrdenSalida(ordenSalida);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        return movimiento;
    }
    /**
     * Guarda una orden simple (sin items, para compatibilidad).
     */
    @Transactional
    public OrdenSalida guardarOrden(OrdenSalida ordenSalida) {
        // Si es una nueva orden, asignar usuario
        if (ordenSalida.getId() == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
            ordenSalida.setUsuario(usuario);
        }

        // Generar números si no existen
        if (ordenSalida.getNumeroOrden() == null || ordenSalida.getNumeroOrden().isEmpty()) {
            ordenSalida.setNumeroOrden(generarNumeroOrden());
        }
        if (ordenSalida.getNumeroOrdenSalida() == null || ordenSalida.getNumeroOrdenSalida().isEmpty()) {
            ordenSalida.setNumeroOrdenSalida(generarNumeroOrdenSalida(ordenSalida.getFechaSalida()));
        }

        // Actualizar fechas
        if (ordenSalida.getFechaRegistro() == null) {
            ordenSalida.setFechaRegistro(LocalDateTime.now());
        }
        ordenSalida.setFechaActualizacion(LocalDateTime.now());

        return ordenSalidaRepository.save(ordenSalida);
    }

    /**
     * Elimina una orden de salida por ID.
     */
    @Transactional
    public void eliminarOrden(Long id) {
        Optional<OrdenSalida> ordenOpt = obtenerOrdenConItems(id);
        if (ordenOpt.isPresent()) {
            OrdenSalida orden = ordenOpt.get();

            // Revertir stock de los items
            for (OrdenSalidaItem item : orden.getItems()) {
                Producto producto = item.getProducto();
                if (producto != null) {
                    producto.setCantidad(producto.getCantidad() + item.getCantidad());
                    productoRepository.save(producto);
                }
            }

            // Eliminar la orden (los items se eliminarán por cascade)
            ordenSalidaRepository.deleteById(id);
        } else {
            throw new RuntimeException("Orden no encontrada con ID: " + id);
        }
    }

    /**
     * Cuenta el número total de órdenes de salida.
     */
    public Long contarTotalOrdenes() {
        return ordenSalidaRepository.count();
    }

    /**
     * Obtiene los items de una orden.
     */
    public List<OrdenSalidaItem> obtenerItemsPorOrdenId(Long ordenId) {
        return ordenSalidaItemRepository.findByOrdenSalidaId(ordenId);
    }

    /**
     * Obtiene los items con productos cargados.
     */
    public List<OrdenSalidaItem> obtenerItemsConProductosPorOrdenId(Long ordenId) {
        return ordenSalidaItemRepository.findItemsConProductosPorOrdenId(ordenId);
    }

    // Métodos auxiliares para generar números
    private String generarNumeroOrden() {
        Long totalOrdenes = contarTotalOrdenes();
        int consecutivo = totalOrdenes.intValue() + 1;
        int año = LocalDate.now().getYear();
        return String.format("OS-%04d-%d", consecutivo, año);
    }

    private String generarNumeroOrdenSalida(LocalDate fechaSalida) {
        String fechaStr = fechaSalida != null ?
                fechaSalida.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")) :
                LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));

        Long secuencia = ordenSalidaRepository.countByFechaSalidaYearAndMonth(
                fechaSalida != null ? fechaSalida.getYear() : LocalDate.now().getYear(),
                fechaSalida != null ? fechaSalida.getMonthValue() : LocalDate.now().getMonthValue()
        );

        return String.format("OS-%s-%04d", fechaStr, secuencia + 1);
    }

    //

}