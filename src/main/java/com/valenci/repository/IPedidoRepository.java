package com.valenci.repository;

import com.valenci.domain.EstadoPedido;
import com.valenci.domain.Pedido;
import java.util.List;
import java.util.Optional;

public interface IPedidoRepository {

    Pedido guardarPedido(Pedido pedido);

    void actualizarPedido(Pedido pedido);

    Optional<Pedido> buscarPedidoPorId(int id);

    List<Pedido> buscarPedidosPorClienteId(int idCliente);

    List<Pedido> buscarTodosLosPedidos();

    List<Pedido> buscarPedidosPorEstado(EstadoPedido estado);

    List<Pedido> buscarPedidosPorFecha(java.time.LocalDate fecha);

    List<Pedido> buscarPedidosPorProducto(int idProducto);
}