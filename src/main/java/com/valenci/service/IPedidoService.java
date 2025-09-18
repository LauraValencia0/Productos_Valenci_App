package com.valenci.service;

import com.valenci.domain.Pedido;
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
}


