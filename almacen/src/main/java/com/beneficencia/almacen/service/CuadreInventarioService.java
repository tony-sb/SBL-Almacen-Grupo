package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.CuadreInventario;
import java.util.List;
import java.util.Optional;

public interface CuadreInventarioService {

    CuadreInventario guardarCuadre(CuadreInventario cuadre);
    List<CuadreInventario> obtenerTodosCuadresOrdenados(); // NUEVO
    Optional<CuadreInventario> obtenerCuadrePorId(Long id);
    void confirmarCuadre(Long id);
    void descartarCuadre(Long id);
}