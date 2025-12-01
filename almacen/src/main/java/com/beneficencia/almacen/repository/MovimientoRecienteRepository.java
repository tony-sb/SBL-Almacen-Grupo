package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.MovimientoReciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para operaciones CRUD y consultas personalizadas de la entidad MovimientoReciente.
 * Proporciona métodos para acceder y consultar los movimientos recientes de salida de productos.
 */
@Repository
public interface MovimientoRecienteRepository extends JpaRepository<MovimientoReciente, Long> {

    /**
     * Obtiene todos los movimientos recientes ordenados por fecha de salida descendente.
     * Útil para mostrar los movimientos más recientes primero en listados y dashboards.
     *
     * @return Lista de movimientos recientes ordenados por fecha descendente
     */
    List<MovimientoReciente> findAllByOrderByFechaSalidaDesc();

    /**
     * Obtiene movimientos recientes con información de productos cargada mediante JOIN FETCH.
     * Optimiza la consulta cargando eagermente la relación con productos para evitar el problema N+1.
     * Ordena los resultados por fecha de salida descendente.
     *
     * @return Lista de movimientos recientes con productos cargados, ordenados por fecha descendente
     */
    @Query("SELECT mr FROM MovimientoReciente mr JOIN FETCH mr.producto ORDER BY mr.fechaSalida DESC")
    List<MovimientoReciente> findMovimientosRecientesConProductos();

    /**
     * Busca movimientos recientes por ID de producto.
     * Útil para obtener el historial de movimientos de un producto específico.
     *
     * @param productoId ID del producto para filtrar los movimientos
     * @return Lista de movimientos recientes del producto especificado
     */
    List<MovimientoReciente> findByProductoId(Long productoId);

    /**
     * Busca movimientos recientes por DNI del beneficiario (búsqueda parcial).
     * Realiza una búsqueda case-sensitive que contiene el DNI proporcionado.
     *
     * @param dni Fragmento del DNI del beneficiario a buscar
     * @return Lista de movimientos recientes que coinciden con el DNI proporcionado
     */
    List<MovimientoReciente> findByDniBeneficiarioContaining(String dni);
}