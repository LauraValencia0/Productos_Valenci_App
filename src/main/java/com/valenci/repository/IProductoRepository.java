package com.valenci.repository;

import com.valenci.domain.Producto;
import java.util.List;
import java.util.Optional;

public interface IProductoRepository {

    void guardarProducto(Producto producto);

    Optional<Producto> buscarProductoPorId(int id);

    List<Producto> buscarTodosLosProductos();

    void eliminarProductoPorId(int id);
}