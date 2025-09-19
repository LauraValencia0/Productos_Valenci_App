package com.valenci.service;

import com.valenci.domain.*;
import com.valenci.repository.IPagoRepository;
import com.valenci.repository.IPedidoRepository;
import com.valenci.repository.IProductoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PedidoServiceImpl implements IPedidoService {

    private final IPedidoRepository pedidoRepository;
    private final IProductoRepository productoRepository;
    private final IPagoRepository pagoRepository;

    public PedidoServiceImpl(IPedidoRepository pedidoRepository, IProductoRepository productoRepository, IPagoRepository pagoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.pagoRepository = pagoRepository;
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
    public List<Pedido> obtenerTodosLosPedidos() {
        return pedidoRepository.buscarTodosLosPedidos();
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

        pedidoRepository.actualizarPedido(pedido);
    }

    @Override
    public void registrarPagoParaPedido(int idPedido, BigDecimal monto, MetodoPago metodoPago) {
        Pedido pedido = pedidoRepository.buscarPedidoPorId(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("El pedido con ID " + idPedido + " no existe."));

        if (pedido.getEstadoPedido() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("El pedido no se encuentra en estado PENDIENTE. Estado actual: " + pedido.getEstadoPedido());
        }

        if (monto.compareTo(pedido.getTotalPedido()) != 0) {
            throw new IllegalArgumentException("El monto del pago (" + monto + ") no coincide con el total del pedido (" + pedido.getTotalPedido() + ").");
        }

        Pago nuevoPago = new Pago(0, pedido, monto, LocalDateTime.now(), metodoPago);
        pagoRepository.guardarPago(nuevoPago);
        System.out.println("Pago registrado exitosamente por un monto de $" + monto);

        pedido.setEstadoPedido(EstadoPedido.PAGADO);
        pedidoRepository.actualizarPedido(pedido);
        System.out.println("El estado del pedido " + idPedido + " ha sido actualizado a PAGADO.");
    }


    @Override
    public void actualizarEstadoPedido(int idPedido, EstadoPedido nuevoEstado) {

        Pedido pedido = pedidoRepository.buscarPedidoPorId(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("El pedido con ID " + idPedido + " no existe."));

        pedido.setEstadoPedido(nuevoEstado);

        pedidoRepository.actualizarPedido(pedido);
        System.out.println("Estado del pedido " + idPedido + " actualizado a " + nuevoEstado);
    }

    @Override
    public List<Pedido> obtenerPedidosPorEstado(EstadoPedido estado) {
        return pedidoRepository.buscarPedidosPorEstado(estado);
    }

    @Override
    public List<Pedido> obtenerPedidosPorFecha(java.time.LocalDate fecha) {
        return pedidoRepository.buscarPedidosPorFecha(fecha);
    }

    @Override
    public List<Pedido> obtenerPedidosPorProducto(int idProducto) {
        return pedidoRepository.buscarPedidosPorProducto(idProducto);
    }

}