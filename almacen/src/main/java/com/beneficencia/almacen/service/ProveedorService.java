package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.Proveedor;
import com.beneficencia.almacen.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de proveedores del almacén.
 * Maneja las operaciones CRUD de proveedores, incluyendo búsquedas
 * por diferentes criterios y validaciones de datos únicos como el RUC.
 */
@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    /**
     * Obtiene todos los proveedores registrados en el sistema.
     *
     * @return Lista completa de todos los proveedores
     */
    public List<Proveedor> obtenerTodosProveedores() {
        return proveedorRepository.findAll();
    }

    /**
     * Obtiene un proveedor por su ID único.
     *
     * @param id ID del proveedor a buscar
     * @return Optional con el proveedor encontrado o vacío si no existe
     */
    public Optional<Proveedor> obtenerProveedorPorId(Long id) {
        return proveedorRepository.findById(id);
    }

    /**
     * Obtiene un proveedor por su RUC único.
     *
     * @param ruc Número de RUC del proveedor a buscar
     * @return Optional con el proveedor encontrado o vacío si no existe
     */
    public Optional<Proveedor> obtenerProveedorPorRuc(String ruc) {
        return proveedorRepository.findByRuc(ruc);
    }

    /**
     * Guarda o actualiza un proveedor en el sistema.
     *
     * @param proveedor Proveedor a guardar o actualizar
     * @return Proveedor guardado con ID y datos actualizados
     */
    public Proveedor guardarProveedor(Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }

    /**
     * Elimina un proveedor del sistema por su ID.
     *
     * @param id ID del proveedor a eliminar
     */
    public void eliminarProveedor(Long id) {
        proveedorRepository.deleteById(id);
    }

    /**
     * Verifica si existe un proveedor con el RUC especificado.
     * Utilizado para validaciones de duplicados durante la creación o actualización.
     *
     * @param ruc Número de RUC a verificar
     * @return true si existe un proveedor con el RUC, false en caso contrario
     */
    public boolean existeProveedorConRuc(String ruc) {
        return proveedorRepository.existsByRuc(ruc);
    }

    /**
     * Busca proveedores por nombre (búsqueda case insensitive).
     * Realiza una búsqueda parcial en el nombre del proveedor.
     *
     * @param nombre Fragmento del nombre del proveedor a buscar
     * @return Lista de proveedores cuyos nombres contienen el término proporcionado
     */
    public List<Proveedor> buscarProveedoresPorNombre(String nombre) {
        return proveedorRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Obtiene todos los proveedores activos del sistema.
     * En esta implementación, todos los proveedores se consideran activos.
     *
     * @return Lista de todos los proveedores registrados
     */
    public List<Proveedor> obtenerProveedoresActivos() {
        return proveedorRepository.findAll();
    }
}