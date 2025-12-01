package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.MovimientoSalida;
import com.beneficencia.almacen.model.Producto;
import com.beneficencia.almacen.model.Usuario;
import com.beneficencia.almacen.repository.MovimientoSalidaRepository;
import com.beneficencia.almacen.repository.ProductoRepository;
import com.beneficencia.almacen.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de movimientos de salida de productos del almacén.
 * Maneja las operaciones de registro, consulta y búsqueda de salidas de productos,
 * incluyendo validaciones de stock y actualización del inventario.
 */
@Service
@Transactional
public class MovimientoSalidaService {

    @Autowired
    private MovimientoSalidaRepository movimientoSalidaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Registra un nuevo movimiento de salida de productos.
     * Realiza validaciones de stock, asigna el usuario autenticado y actualiza el inventario.
     *
     * @param movimientoSalida Objeto MovimientoSalida con los datos de la salida
     * @return MovimientoSalida registrado y persistido en la base de datos
     * @throws RuntimeException si el producto no existe, no hay stock suficiente o el usuario no es válido
     */
    @Transactional
    public MovimientoSalida registrarSalida(MovimientoSalida movimientoSalida) {
        // Validar que el producto existe
        Optional<Producto> productoOpt = productoRepository.findById(movimientoSalida.getProducto().getId());
        if (!productoOpt.isPresent()) {
            throw new RuntimeException("Producto no encontrado");
        }

        Producto producto = productoOpt.get();

        // Validar stock suficiente
        if (producto.getCantidad() < movimientoSalida.getCantidad()) {
            throw new RuntimeException("Stock insuficiente. Stock disponible: " + producto.getCantidad());
        }

        // Obtener usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (!usuarioOpt.isPresent()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();
        movimientoSalida.setUsuario(usuario);

        // Si no hay fecha especificada, usar fecha actual
        if (movimientoSalida.getFechaSalida() == null) {
            movimientoSalida.setFechaSalida(LocalDate.now());
        }

        // Actualizar stock del producto
        producto.setCantidad(producto.getCantidad() - movimientoSalida.getCantidad());
        productoRepository.save(producto);

        return movimientoSalidaRepository.save(movimientoSalida);
    }

    /**
     * Obtiene todas las salidas de productos ordenadas por fecha.
     *
     * @return Lista de todos los movimientos de salida ordenados por fecha descendente
     */
    public List<MovimientoSalida> obtenerTodasLasSalidas() {
        return movimientoSalidaRepository.findAllOrderByFecha();
    }

    /**
     * Busca movimientos de salida dentro de un rango de fechas específico.
     *
     * @param fechaInicio Fecha de inicio del rango (inclusive)
     * @param fechaFin Fecha de fin del rango (inclusive)
     * @return Lista de movimientos de salida dentro del rango de fechas especificado
     */
    public List<MovimientoSalida> buscarPorFecha(LocalDate fechaInicio, LocalDate fechaFin) {
        return movimientoSalidaRepository.findByFechaSalidaBetween(fechaInicio, fechaFin);
    }

    /**
     * Busca movimientos de salida por ID de producto.
     *
     * @param productoId ID del producto para filtrar los movimientos
     * @return Lista de movimientos de salida del producto especificado
     */
    public List<MovimientoSalida> buscarPorProducto(Long productoId) {
        return movimientoSalidaRepository.findByProductoId(productoId);
    }

    /**
     * Busca movimientos de salida por DNI del beneficiario (búsqueda parcial).
     *
     * @param dni Fragmento del DNI del beneficiario a buscar
     * @return Lista de movimientos de salida que coinciden con el DNI proporcionado
     */
    public List<MovimientoSalida> buscarPorDniBeneficiario(String dni) {
        return movimientoSalidaRepository.findByDniBeneficiarioContaining(dni);
    }

    /**
     * Busca movimientos de salida por número de trámite (búsqueda parcial).
     *
     * @param tramite Fragmento del número de trámite a buscar
     * @return Lista de movimientos de salida que coinciden con el número de trámite
     */
    public List<MovimientoSalida> buscarPorNumeroTramite(String tramite) {
        return movimientoSalidaRepository.findByNumeroTramiteContaining(tramite);
    }

    /**
     * Busca movimientos de salida por código o nombre de producto.
     * Realiza una búsqueda case-insensitive en código y nombre del producto.
     *
     * @param busqueda Término de búsqueda para código o nombre de producto
     * @return Lista de movimientos de salida que coinciden con el término de búsqueda
     */
    public List<MovimientoSalida> buscarPorProductoCodigoOrNombre(String busqueda) {
        // Primero obtenemos los productos que coinciden
        List<Producto> productos = productoRepository.findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(busqueda, busqueda);

        // Luego obtenemos los movimientos para esos productos
        return productos.stream()
                .map(producto -> movimientoSalidaRepository.findByProductoId(producto.getId()))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un movimiento de salida por su ID.
     *
     * @param id ID del movimiento a buscar
     * @return Optional con el movimiento encontrado o vacío si no existe
     */
    public Optional<MovimientoSalida> obtenerPorId(Long id) {
        return movimientoSalidaRepository.findById(id);
    }

    /**
     * Obtiene las salidas recientes (últimos 7 días).
     * Útil para dashboards y reportes de actividad reciente.
     *
     * @return Lista de movimientos de salida de los últimos 7 días
     */
    public List<MovimientoSalida> obtenerSalidasRecientes() {
        LocalDate fechaInicio = LocalDate.now().minusDays(7); // Últimos 7 días
        return movimientoSalidaRepository.findByFechaSalidaBetween(fechaInicio, LocalDate.now());
    }
}