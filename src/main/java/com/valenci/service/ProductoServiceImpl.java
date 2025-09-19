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

        if (producto == null || producto.getNombreProducto() == null || producto.getNombreProducto().isEmpty()) {
            throw new IllegalArgumentException("El producto o su nombre no pueden ser nulos.");
        }

        productoRepository.guardarProducto(producto);
    }

    @Override
    public void actualizarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }
        productoRepository.buscarProductoPorId(producto.getIdProducto())
                .orElseThrow(() -> new IllegalArgumentException("El producto con ID " + producto.getIdProducto() + " no existe."));

        productoRepository.guardarProducto(producto);
    }

    @Override
    public void eliminarProducto(int idProducto) {

        productoRepository.buscarProductoPorId(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("El producto con ID " + idProducto + " no existe."));


        productoRepository.eliminarProductoPorId(idProducto);
    }

    @Override
    public Optional<Producto> obtenerProductoPorId(int idProducto) {

        return productoRepository.buscarProductoPorId(idProducto);
    }

    @Override
    public List<Producto> obtenerTodosLosProductos() {

        return productoRepository.buscarTodosLosProductos();
    }
}