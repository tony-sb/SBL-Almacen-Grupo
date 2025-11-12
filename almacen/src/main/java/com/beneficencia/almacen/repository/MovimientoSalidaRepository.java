package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.MovimientoSalida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovimientoSalidaRepository extends JpaRepository<MovimientoSalida, Long> {

    List<MovimientoSalida> findByFechaSalidaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    List<MovimientoSalida> findByProductoId(Long productoId);

    @Query("SELECT ms FROM MovimientoSalida ms WHERE ms.dniBeneficiario LIKE %:dni%")
    List<MovimientoSalida> findByDniBeneficiarioContaining(@Param("dni") String dni);

    @Query("SELECT ms FROM MovimientoSalida ms WHERE ms.numeroTramite LIKE %:tramite%")
    List<MovimientoSalida> findByNumeroTramiteContaining(@Param("tramite") String tramite);

    @Query("SELECT ms FROM MovimientoSalida ms ORDER BY ms.fechaSalida DESC, ms.fechaRegistro DESC")
    List<MovimientoSalida> findAllOrderByFecha();

    @Query("SELECT ms FROM MovimientoSalida ms WHERE ms.producto.codigo LIKE %:codigo% OR ms.producto.nombre LIKE %:nombre%")
    List<MovimientoSalida> findByProductoCodigoOrNombre(@Param("codigo") String codigo, @Param("nombre") String nombre);
}