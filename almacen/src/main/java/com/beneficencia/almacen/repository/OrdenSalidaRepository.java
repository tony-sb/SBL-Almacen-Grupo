package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.OrdenSalida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenSalidaRepository extends JpaRepository<OrdenSalida, Long> {

    List<OrdenSalida> findByFechaSalidaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT os FROM OrdenSalida os ORDER BY os.fechaSalida DESC, os.fechaRegistro DESC")
    List<OrdenSalida> findAllOrderByFecha();

    @Query("SELECT os FROM OrdenSalida os WHERE os.dniUsuario LIKE %:dni%")
    List<OrdenSalida> findByDniUsuarioContaining(@Param("dni") String dni);

    @Query("SELECT os FROM OrdenSalida os WHERE os.numeroTramite LIKE %:tramite%")
    List<OrdenSalida> findByNumeroTramiteContaining(@Param("tramite") String tramite);

    // NUEVO MÉTODO para buscar por número de orden
    @Query("SELECT os FROM OrdenSalida os WHERE os.numeroOrden = :numeroOrden")
    Optional<OrdenSalida> findByNumeroOrden(@Param("numeroOrden") String numeroOrden);
}