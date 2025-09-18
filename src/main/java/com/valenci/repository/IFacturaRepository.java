package com.valenci.repository;

import com.valenci.domain.Factura;
import java.util.Optional;

public interface IFacturaRepository {

    void guardarFactura(Factura factura);

    Optional<Factura> buscarFacturaPorPedidoId(int idPedido);
}