package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.OrdenSalida;
import com.beneficencia.almacen.repository.OrdenSalidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de órdenes de salida de productos del almacén.
 * Maneja las operaciones CRUD de órdenes de salida, incluyendo búsquedas
 * por diferentes criterios y generación de números de orden.
 */
@Service
@Transactional
public class OrdenSalidaService {

    @Autowired
    private OrdenSalidaRepository ordenSalidaRepository;

    /**
     * Obtiene todas las órdenes de salida ordenadas por fecha.
     * Las órdenes se devuelven en orden descendente por fecha de salida y registro.
     *
     * @return Lista de todas las órdenes de salida ordenadas por fecha descendente
     */
    public List<OrdenSalida> obtenerTodasOrdenes() {
        return ordenSalidaRepository.findAllOrderByFecha();
    }

    /**
     * Obtiene una orden de salida por su ID.
     *
     * @param id ID de la orden a buscar
     * @return Optional con la orden encontrada o vacío si no existe
     */
    public Optional<OrdenSalida> obtenerOrdenPorId(Long id) {
        return ordenSalidaRepository.findById(id);
    }

    /**
     * Busca órdenes de salida dentro de un rango de fechas específico.
     *
     * @param fechaInicio Fecha de inicio del rango (inclusive)
     * @param fechaFin Fecha de fin del rango (inclusive)
     * @return Lista de órdenes de salida dentro del rango de fechas especificado
     */
    public List<OrdenSalida> buscarPorFecha(LocalDate fechaInicio, LocalDate fechaFin) {
        return ordenSalidaRepository.findByFechaSalidaBetween(fechaInicio, fechaFin);
    }

    /**
     * Busca órdenes de salida por DNI del usuario (búsqueda parcial).
     *
     * @param dni Fragmento del DNI del usuario a buscar
     * @return Lista de órdenes de salida que coinciden con el DNI proporcionado
     */
    public List<OrdenSalida> buscarPorDniUsuario(String dni) {
        return ordenSalidaRepository.findByDniUsuarioContaining(dni);
    }

    /**
     * Busca órdenes de salida por número de trámite (búsqueda parcial).
     *
     * @param tramite Fragmento del número de trámite a buscar
     * @return Lista de órdenes de salida que coinciden con el número de trámite
     */
    public List<OrdenSalida> buscarPorNumeroTramite(String tramite) {
        return ordenSalidaRepository.findByNumeroTramiteContaining(tramite);
    }

    /**
     * Busca una orden de salida por su número de orden único.
     * Implementación que primero obtiene todas las órdenes y luego filtra.
     *
     * @param numeroOrden Número único de la orden a buscar
     * @return Optional con la orden encontrada o vacío si no existe
     */
    public Optional<OrdenSalida> buscarPorNumeroOrden(String numeroOrden) {
        List<OrdenSalida> todasOrdenes = obtenerTodasOrdenes();
        return todasOrdenes.stream()
                .filter(orden -> orden.getNumeroOrden().equals(numeroOrden))
                .findFirst();
    }

    /**
     * Guarda o actualiza una orden de salida.
     *
     * @param ordenSalida Orden de salida a guardar o actualizar
     * @return Orden de salida guardada con ID y datos actualizados
     */
    public OrdenSalida guardarOrden(OrdenSalida ordenSalida) {
        return ordenSalidaRepository.save(ordenSalida);
    }

    /**
     * Elimina una orden de salida por ID.
     *
     * @param id ID de la orden a eliminar
     */
    public void eliminarOrden(Long id) {
        ordenSalidaRepository.deleteById(id);
    }

    /**
     * Cuenta el número total de órdenes de salida en el sistema.
     * Utilizado para generar números de orden consecutivos automáticamente.
     *
     * @return Número total de órdenes de salida registradas
     */
    public Long contarTotalOrdenes() {
        return ordenSalidaRepository.count();
    }
}