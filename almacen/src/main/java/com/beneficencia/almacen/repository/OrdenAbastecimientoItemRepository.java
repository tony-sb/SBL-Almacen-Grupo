package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.OrdenAbastecimientoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenAbastecimientoItemRepository extends JpaRepository<OrdenAbastecimientoItem, Long> {
}