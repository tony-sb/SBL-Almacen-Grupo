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

    /**
     * Obtener todos los proveedores
     */
    public List<Proveedor> obtenerTodosProveedores() {
        return proveedorRepository.findAll();
    }

    /**
     * Obtener proveedor por ID
     */
    public Optional<Proveedor> obtenerProveedorPorId(Long id) {
        return proveedorRepository.findById(id);
    }

    /**
     * Obtener proveedor por RUC
     */
    public Optional<Proveedor> obtenerProveedorPorRuc(String ruc) {
        return proveedorRepository.findByRuc(ruc);
    }

    /**
     * Guardar o actualizar proveedor
     */
    public Proveedor guardarProveedor(Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }

    /**
     * Eliminar proveedor por ID
     */
    public void eliminarProveedor(Long id) {
        proveedorRepository.deleteById(id);
    }

    /**
     * Verificar si existe un proveedor con el RUC
     */
    public boolean existeProveedorConRuc(String ruc) {
        return proveedorRepository.existsByRuc(ruc);
    }

    /**
     * Buscar proveedores por nombre
     */
    public List<Proveedor> buscarProveedoresPorNombre(String nombre) {
        return proveedorRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Obtener proveedores activos (todos en este caso)
     */
    public List<Proveedor> obtenerProveedoresActivos() {
        return proveedorRepository.findAll();
    }
}