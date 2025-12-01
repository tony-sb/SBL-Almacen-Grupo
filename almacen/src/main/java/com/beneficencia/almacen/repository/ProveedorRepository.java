package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD y consultas personalizadas de la entidad Proveedor.
 * Proporciona métodos para gestionar proveedores del sistema con opciones de búsqueda
 * y validación de datos únicos como el RUC.
 */
@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    /**
     * Busca un proveedor por su RUC único.
     * Utilizado para validar la existencia de proveedores y evitar duplicados.
     *
     * @param ruc Número de RUC del proveedor a buscar
     * @return Optional con el proveedor encontrado o vacío si no existe
     */
    Optional<Proveedor> findByRuc(String ruc);

    /**
     * Busca proveedores por nombre (búsqueda case insensitive).
     * Realiza una búsqueda parcial en el nombre del proveedor,
     * insensible a mayúsculas y minúsculas.
     *
     * @param nombre Fragmento del nombre del proveedor a buscar
     * @return Lista de proveedores cuyos nombres contienen el término proporcionado
     */
    List<Proveedor> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Verifica si existe un proveedor con el RUC especificado.
     * Utilizado para validaciones de duplicados durante la creación o actualización de proveedores.
     *
     * @param ruc Número de RUC a verificar
     * @return true si existe un proveedor con el RUC, false en caso contrario
     */
    boolean existsByRuc(String ruc);
}