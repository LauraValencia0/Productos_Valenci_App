package com.valenci.repository;

import com.valenci.domain.Factura;
import java.util.List; // <-- Nueva importación
import java.util.Optional;

public interface IFacturaRepository {

    void guardarFactura(Factura factura);

    Optional<Factura> buscarFacturaPorPedidoId(int idPedido);

    Optional<Factura> buscarFacturaPorId(int idFactura);

    List<Factura> buscarTodasLasFacturas();

    List<Factura> buscarFacturasPorClienteId(int idCliente);
}