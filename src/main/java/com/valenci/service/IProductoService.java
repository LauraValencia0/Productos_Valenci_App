package com.valenci.service;

import com.valenci.domain.Producto;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define el contrato para la lógica de negocio
 * relacionada con la gestión de productos.
 */
public interface IProductoService {

    /**
     * Agrega un nuevo producto al catálogo.
     * @param producto El producto a agregar.
     */
    void agregarProducto(Producto producto);

    /**
     * Actualiza la información de un producto existente.
     * @param producto El producto con la información actualizada.
     */
    void actualizarProducto(Producto producto);

    /**
     * Elimina un producto del catálogo por su ID.
     * @param idProducto El ID del producto a eliminar.
     */
    void eliminarProducto(int idProducto);

    /**
     * Obtiene un producto específico por su ID.
     * @param idProducto El ID del producto a buscar.
     * @return Un Optional con el producto si se encuentra.
     */
    Optional<Producto> obtenerProductoPorId(int idProducto);

    /**
     * Obtiene la lista completa de productos disponibles.
     * @return Una lista de todos los productos.
     */
    List<Producto> obtenerTodosLosProductos();
}

