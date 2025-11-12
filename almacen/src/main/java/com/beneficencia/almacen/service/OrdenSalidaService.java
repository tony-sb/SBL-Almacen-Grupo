package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.OrdenSalida;
import com.beneficencia.almacen.repository.OrdenSalidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrdenSalidaService {

    @Autowired
    private OrdenSalidaRepository ordenSalidaRepository;

    public List<OrdenSalida> obtenerTodasOrdenes() {
        return ordenSalidaRepository.findAllOrderByFecha();
    }

    public Optional<OrdenSalida> obtenerOrdenPorId(Long id) {
        return ordenSalidaRepository.findById(id);
    }

    public List<OrdenSalida> buscarPorFecha(LocalDate fechaInicio, LocalDate fechaFin) {
        return ordenSalidaRepository.findByFechaSalidaBetween(fechaInicio, fechaFin);
    }

    public List<OrdenSalida> buscarPorDniUsuario(String dni) {
        return ordenSalidaRepository.findByDniUsuarioContaining(dni);
    }

    public List<OrdenSalida> buscarPorNumeroTramite(String tramite) {
        return ordenSalidaRepository.findByNumeroTramiteContaining(tramite);
    }

    // Nuevo método para buscar por número de orden
    public Optional<OrdenSalida> buscarPorNumeroOrden(String numeroOrden) {
        List<OrdenSalida> todasOrdenes = obtenerTodasOrdenes();
        return todasOrdenes.stream()
                .filter(orden -> orden.getNumeroOrden().equals(numeroOrden))
                .findFirst();
    }

    public OrdenSalida guardarOrden(OrdenSalida ordenSalida) {
        return ordenSalidaRepository.save(ordenSalida);
    }

    public void eliminarOrden(Long id) {
        ordenSalidaRepository.deleteById(id);
    }

    public Long contarTotalOrdenes() {
        return ordenSalidaRepository.count();
    }
}