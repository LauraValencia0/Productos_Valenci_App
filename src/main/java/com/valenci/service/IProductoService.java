package com.valenci.service;

import com.valenci.domain.Producto;
import java.util.List;
import java.util.Optional;

public interface IProductoService {

    void agregarProducto(Producto producto);

    void actualizarProducto(Producto producto);

    void eliminarProducto(int idProducto);

    Optional<Producto> obtenerProductoPorId(int idProducto);

    List<Producto> obtenerTodosLosProductos();
}

