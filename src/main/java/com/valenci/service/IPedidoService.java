package com.valenci.service;

import com.valenci.domain.EstadoPedido;
import com.valenci.domain.Factura;
import com.valenci.domain.MetodoPago;
import com.valenci.domain.Pedido;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define el contrato para la lógica de negocio
 * relacionada con la creación y gestión de pedidos.
 */
public interface IPedidoService {

    /**
     * Crea un nuevo pedido en el sistema.
     * Esta operación de negocio debe:
     * 1. Validar el stock de los productos.
     * 2. Calcular el total del pedido.
     * 3. Actualizar el stock de los productos vendidos.
     * 4. Guardar el pedido y sus detalles en la base de datos.
     * @param pedido El objeto Pedido con la información del cliente y los detalles de los productos.
     * @return El pedido creado y guardado.
     */
    Pedido crearPedido(Pedido pedido);

    /**
     * Obtiene un pedido específico por su ID.
     * @param idPedido El ID del pedido a buscar.
     * @return Un Optional con el pedido si se encuentra.
     */
    Optional<Pedido> obtenerPedidoPorId(int idPedido);

    /**
     * Obtiene el historial de pedidos de un cliente específico.
     * @param idCliente El ID del cliente.
     * @return Una lista con todos los pedidos del cliente.
     */
    List<Pedido> obtenerPedidosPorCliente(int idCliente);

    /**
     * Cancela un pedido.
     * La lógica de negocio podría incluir la reposición de stock,
     * la anulación de la factura, etc.
     * @param idPedido El ID del pedido a cancelar.
     */
    void cancelarPedido(int idPedido);

    List<Pedido> obtenerTodosLosPedidos();

    /**
     * Registra un pago para un pedido específico y actualiza su estado a PAGADO.
     * @param idPedido El ID del pedido a pagar.
     * @param monto El monto que se está pagando.
     * @param metodoPago El método de pago utilizado.
     */
    void registrarPagoParaPedido(int idPedido, BigDecimal monto, MetodoPago metodoPago);

    void actualizarEstadoPedido(int idPedido, EstadoPedido nuevoEstado); // <-- NUEVO
    List<Pedido> obtenerPedidosPorEstado(EstadoPedido estado); // <-- NUEVO
    List<Pedido> obtenerPedidosPorFecha(java.time.LocalDate fecha); // <-- NUEVO
    List<Pedido> obtenerPedidosPorProducto(int idProducto);

    // --- LÍNEA AÑADIDA ---
    Optional<Factura> obtenerFacturaPorPedidoId(int idPedido);

    // --- MÉTODOS NUEVOS AÑADIDOS PARA GESTIÓN DE FACTURAS ---
    Optional<Factura> obtenerFacturaPorId(int idFactura);
    List<Factura> obtenerTodasLasFacturas();
    List<Factura> obtenerFacturasPorCliente(int idCliente);
}


