package com.valenci.service;

import com.valenci.domain.DetallePedido;
import com.valenci.domain.EstadoPedido;
import com.valenci.domain.Pedido;
import com.valenci.domain.Producto;
import com.valenci.repository.IPedidoRepository;
import com.valenci.repository.IProductoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PedidoServiceImpl implements IPedidoService {

    private final IPedidoRepository pedidoRepository;
    private final IProductoRepository productoRepository;

    public PedidoServiceImpl(IPedidoRepository pedidoRepository, IProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public Pedido crearPedido(Pedido pedido) {

        if (pedido == null || pedido.getCliente() == null || pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El pedido, cliente o detalles no pueden ser nulos o vacíos.");
        }

        BigDecimal totalGeneral = BigDecimal.ZERO;

        for (DetallePedido detalle : pedido.getDetalles()) {

            int idProducto = detalle.getProducto().getIdProducto();
            Producto productoEnDB = productoRepository.buscarProductoPorId(idProducto)
                    .orElseThrow(() -> new IllegalArgumentException("El producto con ID " + idProducto + " no existe."));

            if (productoEnDB.getCantidad() < detalle.getCantidad()) {
                throw new IllegalStateException("Stock insuficiente para el producto: " + productoEnDB.getNombreProducto());
            }

            detalle.setPrecioUnitario(productoEnDB.getPrecio());
            BigDecimal subtotal = productoEnDB.getPrecio().multiply(BigDecimal.valueOf(detalle.getCantidad()));
            detalle.setSubtotal(subtotal);


            totalGeneral = totalGeneral.add(subtotal);


            int nuevoStock = productoEnDB.getCantidad() - detalle.getCantidad();
            productoEnDB.setCantidad(nuevoStock);
            productoRepository.guardarProducto(productoEnDB);
        }

        pedido.setTotalPedido(totalGeneral);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstadoPedido(EstadoPedido.PENDIENTE);


        return pedidoRepository.guardarPedido(pedido);
    }

    @Override
    public Optional<Pedido> obtenerPedidoPorId(int idPedido) {
        return pedidoRepository.buscarPedidoPorId(idPedido);
    }

    @Override
    public List<Pedido> obtenerPedidosPorCliente(int idCliente) {
        return pedidoRepository.buscarPedidosPorClienteId(idCliente);
    }

    @Override
    public void cancelarPedido(int idPedido) {

        Pedido pedido = pedidoRepository.buscarPedidoPorId(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("El pedido con ID " + idPedido + " no existe."));

        if (pedido.getEstadoPedido() == EstadoPedido.ENVIADO || pedido.getEstadoPedido() == EstadoPedido.ENTREGADO) {
            throw new IllegalStateException("No se puede cancelar un pedido que ya ha sido enviado o entregado.");
        }

        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto productoEnDB = productoRepository.buscarProductoPorId(detalle.getProducto().getIdProducto()).orElse(null);
            if(productoEnDB != null) {
                productoEnDB.setCantidad(productoEnDB.getCantidad() + detalle.getCantidad());
                productoRepository.guardarProducto(productoEnDB);
            }
        }

        pedido.setEstadoPedido(EstadoPedido.CANCELADO);
        pedidoRepository.guardarPedido(pedido);
    }

    @Override
    public List<Pedido> obtenerTodosLosPedidos() {
        return pedidoRepository.buscarTodosLosPedidos();
    }

}