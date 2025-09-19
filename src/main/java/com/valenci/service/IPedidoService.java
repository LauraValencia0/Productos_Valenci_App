package com.valenci.service;

import com.valenci.domain.Pedido;
import java.util.List;
import java.util.Optional;

public interface IPedidoService {

    Pedido crearPedido(Pedido pedido);

    Optional<Pedido> obtenerPedidoPorId(int idPedido);

    List<Pedido> obtenerPedidosPorCliente(int idCliente);

    void cancelarPedido(int idPedido);

    List<Pedido> obtenerTodosLosPedidos();
}


