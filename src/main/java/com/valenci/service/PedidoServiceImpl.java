package com.valenci.service;

import com.valenci.domain.*;
import com.valenci.repository.IPagoRepository;
import com.valenci.repository.IPedidoRepository;
import com.valenci.repository.IProductoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// ... (importaciones) ...
import com.valenci.domain.Factura; // <-- Nueva importación
import com.valenci.repository.IFacturaRepository; // <-- Nueva importación

public class PedidoServiceImpl implements IPedidoService {

    private final IPedidoRepository pedidoRepository;
    private final IProductoRepository productoRepository;
    private final IPagoRepository pagoRepository;
    private final IFacturaRepository facturaRepository;

    public Optional<Factura> obtenerFacturaPorPedidoId(int idPedido) {
        return facturaRepository.buscarFacturaPorPedidoId(idPedido);
    }

    // ACTUALIZAR EL CONSTRUCTOR para inyectar el nuevo repositorio de facturas
    public PedidoServiceImpl(IPedidoRepository pedidoRepository, IProductoRepository productoRepository, IPagoRepository pagoRepository, IFacturaRepository facturaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.pagoRepository = pagoRepository;
        this.facturaRepository = facturaRepository; // <-- ASIGNAR DEPENDENCIA
    }

    // ... método crearPedido() sin cambios ...
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

    // ... métodos de búsqueda sin cambios ...
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

        // --- LÍNEA MODIFICADA ---
        // Usamos el nuevo método más específico para actualizar.
        pedidoRepository.actualizarPedido(pedido);
    }

    @Override
    public void registrarPagoParaPedido(int idPedido, BigDecimal monto, MetodoPago metodoPago) {
        // 1. Buscar y validar el pedido (sin cambios)
        Pedido pedido = pedidoRepository.buscarPedidoPorId(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("El pedido con ID " + idPedido + " no existe."));

        if (pedido.getEstadoPedido() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("El pedido no se encuentra en estado PENDIENTE.");
        }
        if (monto.compareTo(pedido.getTotalPedido()) != 0) {
            throw new IllegalArgumentException("El monto del pago no coincide con el total del pedido.");
        }

        // 2. Crear y guardar el Pago (sin cambios)
        Pago nuevoPago = new Pago(0, pedido, monto, LocalDateTime.now(), metodoPago);
        pagoRepository.guardarPago(nuevoPago);
        System.out.println("Pago registrado exitosamente por un monto de $" + monto);

        // 3. Actualizar el estado del Pedido (sin cambios)
        pedido.setEstadoPedido(EstadoPedido.PAGADO);
        pedidoRepository.actualizarPedido(pedido);
        System.out.println("El estado del pedido " + idPedido + " ha sido actualizado a PAGADO.");

        // --- 4. LÓGICA NUEVA: GENERAR LA FACTURA ---
        try {
            // Suponemos una tasa de IVA del 19% (común en Colombia)
            BigDecimal iva = pedido.getTotalPedido().multiply(new BigDecimal("0.19"));

            Factura nuevaFactura = new Factura(0, pedido, LocalDateTime.now(), pedido.getTotalPedido(), iva);
            facturaRepository.guardarFactura(nuevaFactura);
            System.out.println("Factura N°" + nuevaFactura.getIdFactura() + " generada para el pedido " + idPedido + ".");

        } catch (Exception e) {
            // En un caso real, aquí se manejaría el error de forma más robusta.
            System.out.println("ADVERTENCIA: El pago fue exitoso, pero hubo un error al generar la factura: " + e.getMessage());
        }
    }



    // --- MÉTODOS NUEVOS AÑADIDOS ---

    @Override
    public void actualizarEstadoPedido(int idPedido, EstadoPedido nuevoEstado) {
        // Lógica de negocio: buscamos el pedido para asegurarnos de que existe
        Pedido pedido = pedidoRepository.buscarPedidoPorId(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("El pedido con ID " + idPedido + " no existe."));

        // Actualizamos su estado
        pedido.setEstadoPedido(nuevoEstado);

        // Guardamos el cambio en la base de datos
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

    // --- MÉTODOS NUEVOS AÑADIDOS para manejar las facturas ---

    @Override
    public Optional<Factura> obtenerFacturaPorId(int idFactura) {
        return facturaRepository.buscarFacturaPorId(idFactura);
    }

    @Override
    public List<Factura> obtenerTodasLasFacturas() {
        return facturaRepository.buscarTodasLasFacturas();
    }

    @Override
    public List<Factura> obtenerFacturasPorCliente(int idCliente) {
        return facturaRepository.buscarFacturasPorClienteId(idCliente);
    }

}