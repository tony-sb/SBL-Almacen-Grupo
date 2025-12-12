package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.Beneficiario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiarioRepository extends JpaRepository<Beneficiario, Long> {
    Optional<Beneficiario> findByDni(String dni);
    boolean existsByDni(String dni);

    @Query("SELECT b FROM Beneficiario b ORDER BY b.fechaRegistro DESC")
    List<Beneficiario> findAllOrderByFechaRegistroDesc();
}