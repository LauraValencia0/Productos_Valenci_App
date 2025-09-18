package com.valenci.repository;

import com.valenci.domain.Pedido;
import java.util.List;
import java.util.Optional;

public interface IPedidoRepository {

    Pedido guardarPedido(Pedido pedido);

    Optional<Pedido> buscarPedidoPorId(int id);

    List<Pedido> buscarPedidosPorClienteId(int idCliente);

    List<Pedido> buscarTodosLosPedidos();
}