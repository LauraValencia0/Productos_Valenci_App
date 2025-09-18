package com.valenci.service;

import com.valenci.domain.Producto;
import com.valenci.repository.IProductoRepository;

import java.util.List;
import java.util.Optional;

public class ProductoServiceImpl implements IProductoService {

    private final IProductoRepository productoRepository;

    public ProductoServiceImpl(IProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public void agregarProducto(Producto producto) {
        // Lógica de negocio antes de agregar un producto (ej. validaciones)
        if (producto == null || producto.getNombreProducto() == null || producto.getNombreProducto().isEmpty()) {
            throw new IllegalArgumentException("El producto o su nombre no pueden ser nulos.");
        }
        // Se delega la acción de guardar al repositorio.
        productoRepository.guardarProducto(producto);
    }

    @Override
    public void actualizarProducto(Producto producto) {
        // Lógica de negocio: Asegurarse de que el producto a actualizar existe.
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }
        productoRepository.buscarProductoPorId(producto.getIdProducto())
                .orElseThrow(() -> new IllegalArgumentException("El producto con ID " + producto.getIdProducto() + " no existe."));

        // Si existe, se procede a guardar los cambios.
        productoRepository.guardarProducto(producto);
    }

    @Override
    public void eliminarProducto(int idProducto) {
        // Lógica de negocio: Asegurarse de que el producto a eliminar existe.
        productoRepository.buscarProductoPorId(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("El producto con ID " + idProducto + " no existe."));

        // Si existe, se elimina.
        productoRepository.eliminarProductoPorId(idProducto);
    }

    @Override
    public Optional<Producto> obtenerProductoPorId(int idProducto) {
        // Se delega la llamada directamente al repositorio.
        return productoRepository.buscarProductoPorId(idProducto);
    }

    @Override
    public List<Producto> obtenerTodosLosProductos() {
        // Se delega la llamada directamente al repositorio.
        return productoRepository.buscarTodosLosProductos();
    }
}