package com.beneficencia.almacen.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.*;

@Service
@Transactional
public class EstadisticaService {

    @Autowired
    private EntityManager entityManager;


    public List<Map<String, Object>> obtenerProductosMasSolicitados() {
        try {
            String sql = "SELECT " +
                    "    p.nombre AS producto, " +
                    "    SUM(osi.cantidad) AS cantidad_total, " +
                    "    p.categoria AS categoria " +
                    "FROM orden_salida_items osi " +
                    "JOIN productos p ON osi.producto_id = p.id " +
                    "GROUP BY p.id, p.nombre, p.categoria " +
                    "ORDER BY cantidad_total DESC " +
                    "LIMIT 10";

            Query query = entityManager.createNativeQuery(sql);
            List<Object[]> resultados = query.getResultList();

            List<Map<String, Object>> estadisticas = new ArrayList<>();
            for (Object[] fila : resultados) {
                Map<String, Object> dato = new HashMap<>();
                dato.put("producto", fila[0] != null ? fila[0].toString() : "Sin nombre");
                dato.put("cantidadTotal", fila[1] != null ? ((Number) fila[1]).intValue() : 0);
                dato.put("categoria", fila[2] != null ? fila[2].toString() : "Sin categoría");
                estadisticas.add(dato);
            }

            System.out.println("Productos más solicitados encontrados: " + estadisticas.size());
            return estadisticas;

        } catch (Exception e) {
            System.err.println("Error en obtenerProductosMasSolicitados: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    public List<Map<String, Object>> obtenerBeneficiariosMasActivos() {
        try {
            String sql = "SELECT " +
                    "    CONCAT(b.nombres, ' ', b.apellidos) AS beneficiario, " +
                    "    b.dni AS dni, " +
                    "    COUNT(DISTINCT os.id) AS total_ordenes, " +
                    "    COALESCE(SUM(osi.cantidad), 0) AS total_productos " +
                    "FROM beneficiarios b " +
                    "LEFT JOIN ordenes_salida os ON b.id = os.beneficiario_id " +
                    "LEFT JOIN orden_salida_items osi ON os.id = osi.orden_salida_id " +
                    "GROUP BY b.id, b.nombres, b.apellidos, b.dni " +
                    "HAVING total_productos > 0 " +
                    "ORDER BY total_productos DESC " +
                    "LIMIT 10";

            Query query = entityManager.createNativeQuery(sql);
            List<Object[]> resultados = query.getResultList();

            List<Map<String, Object>> estadisticas = new ArrayList<>();
            for (Object[] fila : resultados) {
                Map<String, Object> dato = new HashMap<>();
                dato.put("beneficiario", fila[0] != null ? fila[0].toString() : "Sin nombre");
                dato.put("dni", fila[1] != null ? fila[1].toString() : "00000000");
                dato.put("totalOrdenes", fila[2] != null ? ((Number) fila[2]).intValue() : 0);
                dato.put("totalProductos", fila[3] != null ? ((Number) fila[3]).intValue() : 0);
                estadisticas.add(dato);
            }

            System.out.println(" Beneficiarios más activos encontrados: " + estadisticas.size());
            return estadisticas;

        } catch (Exception e) {
            System.err.println(" Error en obtenerBeneficiariosMasActivos: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    public List<Map<String, Object>> obtenerEntregasPorMes() {
        try {
            String sql = "SELECT " +
                    "    MONTH(os.fecha_salida) AS mes_numero, " +
                    "    CASE MONTH(os.fecha_salida) " +
                    "        WHEN 1 THEN 'Enero' " +
                    "        WHEN 2 THEN 'Febrero' " +
                    "        WHEN 3 THEN 'Marzo' " +
                    "        WHEN 4 THEN 'Abril' " +
                    "        WHEN 5 THEN 'Mayo' " +
                    "        WHEN 6 THEN 'Junio' " +
                    "        WHEN 7 THEN 'Julio' " +
                    "        WHEN 8 THEN 'Agosto' " +
                    "        WHEN 9 THEN 'Septiembre' " +
                    "        WHEN 10 THEN 'Octubre' " +
                    "        WHEN 11 THEN 'Noviembre' " +
                    "        WHEN 12 THEN 'Diciembre' " +
                    "    END AS mes_nombre, " +
                    "    COUNT(DISTINCT os.id) AS total_entregas, " +
                    "    COALESCE(SUM(osi.cantidad), 0) AS total_productos " +
                    "FROM ordenes_salida os " +
                    "LEFT JOIN orden_salida_items osi ON os.id = osi.orden_salida_id " +
                    "WHERE YEAR(os.fecha_salida) = YEAR(CURDATE()) " +
                    "GROUP BY MONTH(os.fecha_salida) " +
                    "ORDER BY mes_numero";

            Query query = entityManager.createNativeQuery(sql);
            List<Object[]> resultados = query.getResultList();

            List<Map<String, Object>> estadisticas = new ArrayList<>();
            for (Object[] fila : resultados) {
                Map<String, Object> dato = new HashMap<>();
                dato.put("mesNumero", fila[0] != null ? ((Number) fila[0]).intValue() : 0);
                dato.put("mesNombre", fila[1] != null ? fila[1].toString() : "Sin mes");
                dato.put("totalEntregas", fila[2] != null ? ((Number) fila[2]).intValue() : 0);
                dato.put("totalProductos", fila[3] != null ? ((Number) fila[3]).intValue() : 0);
                estadisticas.add(dato);
            }

            System.out.println(" Entregas por mes encontradas: " + estadisticas.size());
            return estadisticas;

        } catch (Exception e) {
            System.err.println(" Error en obtenerEntregasPorMes: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    public Long contarTotalBeneficiarios() {
        try {
            String sql = "SELECT COUNT(*) FROM beneficiarios";
            Query query = entityManager.createNativeQuery(sql);
            Object resultado = query.getSingleResult();
            return resultado != null ? ((Number) resultado).longValue() : 0L;
        } catch (Exception e) {
            System.err.println(" Error en contarTotalBeneficiarios: " + e.getMessage());
            return 0L;
        }
    }

    public Long contarTotalProductosEntregados() {
        try {
            String sql = "SELECT COALESCE(SUM(cantidad), 0) FROM orden_salida_items";
            Query query = entityManager.createNativeQuery(sql);
            Object resultado = query.getSingleResult();
            return resultado != null ? ((Number) resultado).longValue() : 0L;
        } catch (Exception e) {
            System.err.println("Error en contarTotalProductosEntregados: " + e.getMessage());
            return 0L;
        }
    }

    public String obtenerMesConMasEntregas() {
        try {
            String sql = "SELECT " +
                    "    CASE MONTH(fecha_salida) " +
                    "        WHEN 1 THEN 'Enero' " +
                    "        WHEN 2 THEN 'Febrero' " +
                    "        WHEN 3 THEN 'Marzo' " +
                    "        WHEN 4 THEN 'Abril' " +
                    "        WHEN 5 THEN 'Mayo' " +
                    "        WHEN 6 THEN 'Junio' " +
                    "        WHEN 7 THEN 'Julio' " +
                    "        WHEN 8 THEN 'Agosto' " +
                    "        WHEN 9 THEN 'Septiembre' " +
                    "        WHEN 10 THEN 'Octubre' " +
                    "        WHEN 11 THEN 'Noviembre' " +
                    "        WHEN 12 THEN 'Diciembre' " +
                    "    END AS mes " +
                    "FROM ordenes_salida " +
                    "WHERE YEAR(fecha_salida) = YEAR(CURDATE()) " +
                    "GROUP BY MONTH(fecha_salida) " +
                    "ORDER BY COUNT(id) DESC " +
                    "LIMIT 1";

            Query query = entityManager.createNativeQuery(sql);
            Object resultado = query.getSingleResult();
            return resultado != null ? resultado.toString() : "Sin datos";
        } catch (Exception e) {
            System.err.println(" Error en obtenerMesConMasEntregas: " + e.getMessage());
            return "Sin datos";
        }
    }

    /**
     * NUEVO: Obtiene estadísticas generales para debug
     */
    public Map<String, Object> obtenerEstadisticasDebug() {
        Map<String, Object> debug = new HashMap<>();
        try {
            // Verificar conteo de datos
            String sql1 = "SELECT COUNT(*) FROM ordenes_salida";
            String sql2 = "SELECT COUNT(*) FROM orden_salida_items";
            String sql3 = "SELECT COUNT(*) FROM beneficiarios";
            String sql4 = "SELECT COUNT(*) FROM productos";

            Query q1 = entityManager.createNativeQuery(sql1);
            Query q2 = entityManager.createNativeQuery(sql2);
            Query q3 = entityManager.createNativeQuery(sql3);
            Query q4 = entityManager.createNativeQuery(sql4);

            debug.put("totalOrdenesSalida", ((Number) q1.getSingleResult()).longValue());
            debug.put("totalItemsSalida", ((Number) q2.getSingleResult()).longValue());
            debug.put("totalBeneficiarios", ((Number) q3.getSingleResult()).longValue());
            debug.put("totalProductos", ((Number) q4.getSingleResult()).longValue());

            // Verificar si hay datos en orden_salida_items
            String sqlCheck = "SELECT osi.id, p.nombre, osi.cantidad, b.nombres " +
                    "FROM orden_salida_items osi " +
                    "JOIN productos p ON osi.producto_id = p.id " +
                    "JOIN ordenes_salida os ON osi.orden_salida_id = os.id " +
                    "LEFT JOIN beneficiarios b ON os.beneficiario_id = b.id " +
                    "LIMIT 5";

            Query qCheck = entityManager.createNativeQuery(sqlCheck);
            List<Object[]> datos = qCheck.getResultList();
            debug.put("muestraDatos", datos);

        } catch (Exception e) {
            debug.put("error", e.getMessage());
        }
        return debug;
    }
}