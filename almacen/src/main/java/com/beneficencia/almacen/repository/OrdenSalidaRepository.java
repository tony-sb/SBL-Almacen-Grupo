package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.OrdenSalida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD y consultas personalizadas de la entidad OrdenSalida.
 * Proporciona métodos para acceder y consultar las órdenes de salida de productos
 */
@Repository
public interface OrdenSalidaRepository extends JpaRepository<OrdenSalida, Long> {

    /**
     * Busca órdenes de salida dentro de un rango de fechas específico.
     * Útil para generar reportes por período y análisis temporales.
     *
     * @param fechaInicio Fecha de inicio del rango (inclusive)
     * @param fechaFin Fecha de fin del rango (inclusive)
     * @return Lista de órdenes de salida dentro del rango de fechas especificado
     */
    List<OrdenSalida> findByFechaSalidaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Obtiene todas las órdenes de salida ordenadas por fecha de salida y fecha de registro descendente.
     * Proporciona una vista cronológica inversa de las órdenes, mostrando las más recientes primero.
     *
     * @return Lista de todas las órdenes de salida ordenadas por fecha descendente
     */
    @Query("SELECT os FROM OrdenSalida os ORDER BY os.fechaSalida DESC, os.fechaRegistro DESC")
    List<OrdenSalida> findAllOrderByFecha();

    /**
     * Busca órdenes de salida por DNI del usuario (búsqueda parcial).
     * Realiza una búsqueda case-sensitive que contiene el DNI proporcionado.
     *
     * @param dni Fragmento del DNI del usuario a buscar
     * @return Lista de órdenes de salida que coinciden con el DNI proporcionado
     */
    @Query("SELECT os FROM OrdenSalida os WHERE os.dniUsuario LIKE %:dni%")
    List<OrdenSalida> findByDniUsuarioContaining(@Param("dni") String dni);

    /**
     * Busca órdenes de salida por número de trámite (búsqueda parcial).
     * Permite localizar órdenes relacionadas con trámites específicos.
     *
     * @param tramite Fragmento del número de trámite a buscar
     * @return Lista de órdenes de salida que coinciden con el número de trámite
     */
    @Query("SELECT os FROM OrdenSalida os WHERE os.numeroTramite LIKE %:tramite%")
    List<OrdenSalida> findByNumeroTramiteContaining(@Param("tramite") String tramite);

    /**
     * Busca una orden de salida por su número de orden único.
     * Utilizado para operaciones de búsqueda específica y validación de existencia.
     *
     * @param numeroOrden Número único de la orden a buscar
     * @return Optional con la orden encontrada o vacío si no existe
     */
    @Query("SELECT os FROM OrdenSalida os WHERE os.numeroOrden = :numeroOrden")
    Optional<OrdenSalida> findByNumeroOrden(@Param("numeroOrden") String numeroOrden);
}