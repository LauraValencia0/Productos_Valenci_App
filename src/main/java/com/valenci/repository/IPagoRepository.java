package com.valenci.repository;

import com.valenci.domain.Pago;
import java.util.List;

public interface IPagoRepository {

    void guardarPago(Pago pago);

    List<Pago> buscarPagosPorPedidoId(int idPedido);
}