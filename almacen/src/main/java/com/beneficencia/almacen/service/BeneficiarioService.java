package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.Beneficiario;
import com.beneficencia.almacen.repository.BeneficiarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BeneficiarioService {

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    public Optional<Beneficiario> obtenerBeneficiarioPorDni(String dni) {
        return beneficiarioRepository.findByDni(dni);
    }

    public List<Beneficiario> obtenerTodosBeneficiarios() {
        return beneficiarioRepository.findAll();
    }

    public Optional<Beneficiario> obtenerBeneficiarioPorId(Long id) {
        return beneficiarioRepository.findById(id);
    }

    public Optional<Beneficiario> buscarPorDni(String dni) {
        return beneficiarioRepository.findByDni(dni);
    }

    public Beneficiario guardarBeneficiario(Beneficiario beneficiario) {
        // Establecer fechas
        if (beneficiario.getId() == null) {
            beneficiario.setFechaRegistro(LocalDateTime.now());
        }
        beneficiario.setFechaActualizacion(LocalDateTime.now());

        return beneficiarioRepository.save(beneficiario);
    }

    public void eliminarBeneficiario(Long id) {
        beneficiarioRepository.deleteById(id);
    }

    public boolean existePorDni(String dni) {
        return beneficiarioRepository.existsByDni(dni);
    }

    public long contarTotalBeneficiarios() {
        return beneficiarioRepository.count();
    }

    public Beneficiario crearOEncontrarBeneficiario(String dni, String nombreCompleto) {
        if (dni == null || dni.trim().isEmpty()) {
            return null;
        }

        Optional<Beneficiario> existente = beneficiarioRepository.findByDni(dni.trim());
        if (existente.isPresent()) {
            return existente.get();
        }

        Beneficiario nuevo = new Beneficiario();
        nuevo.setDni(dni.trim());

        if (nombreCompleto != null && !nombreCompleto.trim().isEmpty()) {
            String nombre = nombreCompleto.trim();
            if (nombre.contains(" ")) {
                int ultimoEspacio = nombre.lastIndexOf(" ");
                if (ultimoEspacio > 0) {
                    nuevo.setNombres(nombre.substring(0, ultimoEspacio).trim());
                    nuevo.setApellidos(nombre.substring(ultimoEspacio + 1).trim());
                } else {
                    nuevo.setNombres(nombre);
                    nuevo.setApellidos("");
                }
            } else {
                nuevo.setNombres(nombre);
                nuevo.setApellidos("");
            }
        } else {
            nuevo.setNombres("SIN NOMBRE");
            nuevo.setApellidos("");
        }

        return guardarBeneficiario(nuevo);
    }

    public List<Beneficiario> obtenerBeneficiariosOrdenadosPorFechaDesc() {
        return beneficiarioRepository.findAllOrderByFechaRegistroDesc();
    }
}