package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.Proveedor;
import com.beneficencia.almacen.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    public List<Proveedor> obtenerTodosProveedores() {
        return proveedorRepository.findAll();
    }

    public Optional<Proveedor> obtenerProveedorPorId(Long id) {
        return proveedorRepository.findById(id);
    }

    public Optional<Proveedor> obtenerProveedorPorRuc(String ruc) {
        return proveedorRepository.findByRuc(ruc);
    }

    public Proveedor guardarProveedor(Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }

    public void eliminarProveedor(Long id) {
        proveedorRepository.deleteById(id);
    }

    public boolean existeProveedorConRuc(String ruc) {
        return proveedorRepository.existsByRuc(ruc);
    }

    public List<Proveedor> buscarProveedoresPorNombre(String nombre) {
        return proveedorRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public List<Proveedor> obtenerProveedoresActivos() {
        return proveedorRepository.findAll();
    }
}