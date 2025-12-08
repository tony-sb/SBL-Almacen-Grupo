package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.Beneficiario;
import com.beneficencia.almacen.repository.BeneficiarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BeneficiarioService {

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    /**
     * Obtiene todos los beneficiarios registrados.
     */
    public List<Beneficiario> obtenerTodosBeneficiarios() {
        return beneficiarioRepository.findAll();
    }

    /**
     * Busca un beneficiario por su ID.
     */
    public Optional<Beneficiario> obtenerBeneficiarioPorId(Long id) {
        return beneficiarioRepository.findById(id);
    }

    /**
     * Busca un beneficiario por su DNI.
     */
    public Optional<Beneficiario> buscarPorDni(String dni) {
        return beneficiarioRepository.findByDni(dni);
    }

    /**
     * Guarda un beneficiario (crea o actualiza).
     */
    public Beneficiario guardarBeneficiario(Beneficiario beneficiario) {
        return beneficiarioRepository.save(beneficiario);
    }

    /**
     * Elimina un beneficiario por ID.
     */
    public void eliminarBeneficiario(Long id) {
        beneficiarioRepository.deleteById(id);
    }

    /**
     * Verifica si existe un beneficiario con el DNI especificado.
     */
    public boolean existePorDni(String dni) {
        return beneficiarioRepository.existsByDni(dni);
    }
}