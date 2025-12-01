package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.MovimientoSalida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para operaciones CRUD y consultas personalizadas de la entidad MovimientoSalida.
 * Proporciona métodos para acceder y consultar los movimientos de salida de productos
 * con diversas opciones de filtrado y búsqueda.
 */
@Repository
public interface MovimientoSalidaRepository extends JpaRepository<MovimientoSalida, Long> {

    /**
     * Busca movimientos de salida dentro de un rango de fechas específico.
     * Útil para generar reportes por período y análisis temporales.
     *
     * @param fechaInicio Fecha de inicio del rango (inclusive)
     * @param fechaFin Fecha de fin del rango (inclusive)
     * @return Lista de movimientos de salida dentro del rango de fechas especificado
     */
    List<MovimientoSalida> findByFechaSalidaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Busca movimientos de salida por ID de producto.
     * Permite obtener el historial completo de salidas de un producto específico.
     *
     * @param productoId ID del producto para filtrar los movimientos
     * @return Lista de movimientos de salida del producto especificado
     */
    List<MovimientoSalida> findByProductoId(Long productoId);

    /**
     * Busca movimientos de salida por DNI del beneficiario (búsqueda parcial).
     * Realiza una búsqueda case-sensitive que contiene el DNI proporcionado.
     *
     * @param dni Fragmento del DNI del beneficiario a buscar
     * @return Lista de movimientos de salida que coinciden con el DNI proporcionado
     */
    @Query("SELECT ms FROM MovimientoSalida ms WHERE ms.dniBeneficiario LIKE %:dni%")
    List<MovimientoSalida> findByDniBeneficiarioContaining(@Param("dni") String dni);

    /**
     * Busca movimientos de salida por número de trámite (búsqueda parcial).
     * Permite localizar movimientos relacionados con trámites específicos.
     *
     * @param tramite Fragmento del número de trámite a buscar
     * @return Lista de movimientos de salida que coinciden con el número de trámite
     */
    @Query("SELECT ms FROM MovimientoSalida ms WHERE ms.numeroTramite LIKE %:tramite%")
    List<MovimientoSalida> findByNumeroTramiteContaining(@Param("tramite") String tramite);

    /**
     * Obtiene todos los movimientos de salida ordenados por fecha de salida y fecha de registro descendente.
     * Proporciona una vista cronológica inversa de los movimientos, mostrando los más recientes primero.
     *
     * @return Lista de todos los movimientos de salida ordenados por fecha descendente
     */
    @Query("SELECT ms FROM MovimientoSalida ms ORDER BY ms.fechaSalida DESC, ms.fechaRegistro DESC")
    List<MovimientoSalida> findAllOrderByFecha();

    /**
     * Busca movimientos de salida por código o nombre de producto (búsqueda parcial).
     * Permite encontrar movimientos relacionados con productos específicos mediante
     * búsqueda en código o nombre del producto.
     *
     * @param codigo Fragmento del código del producto a buscar
     * @param nombre Fragmento del nombre del producto a buscar
     * @return Lista de movimientos de salida que coinciden con el código o nombre del producto
     */
    @Query("SELECT ms FROM MovimientoSalida ms WHERE ms.producto.codigo LIKE %:codigo% OR ms.producto.nombre LIKE %:nombre%")
    List<MovimientoSalida> findByProductoCodigoOrNombre(@Param("codigo") String codigo, @Param("nombre") String nombre);
}