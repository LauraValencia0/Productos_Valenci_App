package com.valenci.service;

import com.valenci.domain.EstadoPedido;
import com.valenci.domain.MetodoPago;
import com.valenci.domain.Pedido;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IPedidoService {

    Pedido crearPedido(Pedido pedido);
    Optional<Pedido> obtenerPedidoPorId(int idPedido);
    List<Pedido> obtenerPedidosPorCliente(int idCliente);
    void cancelarPedido(int idPedido);
    List<Pedido> obtenerTodosLosPedidos();
    void registrarPagoParaPedido(int idPedido, BigDecimal monto, MetodoPago metodoPago);
    void actualizarEstadoPedido(int idPedido, EstadoPedido nuevoEstado);
    List<Pedido> obtenerPedidosPorEstado(EstadoPedido estado);
    List<Pedido> obtenerPedidosPorFecha(java.time.LocalDate fecha);
    List<Pedido> obtenerPedidosPorProducto(int idProducto);

}


