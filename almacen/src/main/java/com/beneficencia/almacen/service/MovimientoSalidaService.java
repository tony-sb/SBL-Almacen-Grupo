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

@Service
@Transactional
public class MovimientoSalidaService {

    @Autowired
    private MovimientoSalidaRepository movimientoSalidaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public MovimientoSalida registrarSalida(MovimientoSalida movimientoSalida) {

        Optional<Producto> productoOpt = productoRepository.findById(movimientoSalida.getProducto().getId());
        if (!productoOpt.isPresent()) {
            throw new RuntimeException("Producto no encontrado");
        }

        Producto producto = productoOpt.get();

        if (producto.getCantidad() < movimientoSalida.getCantidad()) {
            throw new RuntimeException("Stock insuficiente. Stock disponible: " + producto.getCantidad());
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (!usuarioOpt.isPresent()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();
        movimientoSalida.setUsuario(usuario);

        if (movimientoSalida.getFechaSalida() == null) {
            movimientoSalida.setFechaSalida(LocalDate.now());
        }

        producto.setCantidad(producto.getCantidad() - movimientoSalida.getCantidad());
        productoRepository.save(producto);

        return movimientoSalidaRepository.save(movimientoSalida);
    }

    public List<MovimientoSalida> obtenerTodasLasSalidas() {
        return movimientoSalidaRepository.findAllOrderByFecha();
    }

    public List<MovimientoSalida> buscarPorFecha(LocalDate fechaInicio, LocalDate fechaFin) {
        return movimientoSalidaRepository.findByFechaSalidaBetween(fechaInicio, fechaFin);
    }

    public List<MovimientoSalida> buscarPorProducto(Long productoId) {
        return movimientoSalidaRepository.findByProductoId(productoId);
    }

    public List<MovimientoSalida> buscarPorDniBeneficiario(String dni) {
        return movimientoSalidaRepository.findByDniBeneficiarioContaining(dni);
    }

    public List<MovimientoSalida> buscarPorNumeroTramite(String tramite) {
        return movimientoSalidaRepository.findByNumeroTramiteContaining(tramite);
    }

    public List<MovimientoSalida> buscarPorProductoCodigoOrNombre(String busqueda) {

        List<Producto> productos = productoRepository.findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(busqueda, busqueda);

        return productos.stream()
                .map(producto -> movimientoSalidaRepository.findByProductoId(producto.getId()))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public Optional<MovimientoSalida> obtenerPorId(Long id) {
        return movimientoSalidaRepository.findById(id);
    }

    public List<MovimientoSalida> obtenerSalidasRecientes() {
        LocalDate fechaInicio = LocalDate.now().minusDays(7);
        return movimientoSalidaRepository.findByFechaSalidaBetween(fechaInicio, LocalDate.now());
    }
}