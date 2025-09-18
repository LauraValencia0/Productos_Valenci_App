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

    // Inyectamos todas las dependencias que el servicio necesita para orquestar la lógica.
    public PedidoServiceImpl(IPedidoRepository pedidoRepository, IProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public Pedido crearPedido(Pedido pedido) {
        // --- INICIO DE LÓGICA DE NEGOCIO CRÍTICA ---

        // 1. Validaciones iniciales
        if (pedido == null || pedido.getCliente() == null || pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El pedido, cliente o detalles no pueden ser nulos o vacíos.");
        }

        BigDecimal totalGeneral = BigDecimal.ZERO;

        // 2. Procesar cada línea de detalle del pedido
        for (DetallePedido detalle : pedido.getDetalles()) {
            // Obtenemos el producto desde la BD para asegurar que usamos el precio y stock correctos.
            int idProducto = detalle.getProducto().getIdProducto();
            Producto productoEnDB = productoRepository.buscarProductoPorId(idProducto)
                    .orElseThrow(() -> new IllegalArgumentException("El producto con ID " + idProducto + " no existe."));

            // 3. Validación de stock
            if (productoEnDB.getCantidad() < detalle.getCantidad()) {
                throw new IllegalStateException("Stock insuficiente para el producto: " + productoEnDB.getNombreProducto());
            }

            // 4. Se asegura el precio desde el servidor y se calcula el subtotal
            detalle.setPrecioUnitario(productoEnDB.getPrecio());
            BigDecimal subtotal = productoEnDB.getPrecio().multiply(BigDecimal.valueOf(detalle.getCantidad()));
            detalle.setSubtotal(subtotal);

            // 5. Se acumula al total general
            totalGeneral = totalGeneral.add(subtotal);

            // 6. Se actualiza el stock del producto
            int nuevoStock = productoEnDB.getCantidad() - detalle.getCantidad();
            productoEnDB.setCantidad(nuevoStock);
            productoRepository.guardarProducto(productoEnDB); // Guardamos el producto con el stock actualizado
        }

        // 7. Se finaliza el objeto Pedido con los datos calculados
        pedido.setTotalPedido(totalGeneral);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstadoPedido(EstadoPedido.PENDIENTE);

        // --- FIN DE LÓGICA DE NEGOCIO ---

        // 8. Se guarda el pedido completo (cabecera y detalles) en la BD.
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
        // Lógica de negocio para cancelar un pedido.
        Pedido pedido = pedidoRepository.buscarPedidoPorId(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("El pedido con ID " + idPedido + " no existe."));

        // Solo se pueden cancelar pedidos en ciertos estados.
        if (pedido.getEstadoPedido() == EstadoPedido.ENVIADO || pedido.getEstadoPedido() == EstadoPedido.ENTREGADO) {
            throw new IllegalStateException("No se puede cancelar un pedido que ya ha sido enviado o entregado.");
        }

        // Lógica para reponer el stock...
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
}