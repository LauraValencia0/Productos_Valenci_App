package com.valenci.repository;

import com.valenci.domain.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PedidoRepositoryImpl implements IPedidoRepository {

    private final Connection connection;

    public PedidoRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Pedido guardarPedido(Pedido pedido) {
        String sqlPedido = "INSERT INTO pedidos (fecha_pedido, total_pedido, estado_pedido, id_cliente) VALUES (?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";

        try {
            // --- INICIO DE LA TRANSACCIÓN ---
            connection.setAutoCommit(false);

            // 1. Insertar la cabecera del pedido para obtener su ID
            try (PreparedStatement psPedido = connection.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                psPedido.setTimestamp(1, Timestamp.valueOf(pedido.getFechaPedido()));
                psPedido.setBigDecimal(2, pedido.getTotalPedido());
                psPedido.setString(3, pedido.getEstadoPedido().name());
                psPedido.setInt(4, pedido.getCliente().getId());
                psPedido.executeUpdate();

                // Obtener el ID del pedido recién creado
                try (ResultSet rs = psPedido.getGeneratedKeys()) {
                    if (rs.next()) {
                        pedido.setIdPedido(rs.getInt(1));
                    } else {
                        throw new SQLException("No se pudo obtener el ID del pedido creado.");
                    }
                }
            }

            // 2. Insertar cada uno de los detalles del pedido
            try (PreparedStatement psDetalle = connection.prepareStatement(sqlDetalle)) {
                for (DetallePedido detalle : pedido.getDetalles()) {
                    psDetalle.setInt(1, pedido.getIdPedido());
                    psDetalle.setInt(2, detalle.getProducto().getIdProducto());
                    psDetalle.setInt(3, detalle.getCantidad());
                    psDetalle.setBigDecimal(4, detalle.getPrecioUnitario());
                    psDetalle.setBigDecimal(5, detalle.getSubtotal());
                    psDetalle.addBatch(); // Agregamos la inserción a un lote para eficiencia
                }
                psDetalle.executeBatch(); // Ejecutamos todas las inserciones de detalles juntas
            }

            // Si todo fue exitoso, confirmamos la transacción
            connection.commit();
            // --- FIN DE LA TRANSACCIÓN ---

        } catch (SQLException e) {
            // Si algo falló, deshacemos todos los cambios
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new RuntimeException("Error crítico al intentar hacer rollback de la transacción", rollbackEx);
            }
            throw new RuntimeException("Error al guardar el pedido", e);
        } finally {
            // Devolvemos la conexión a su estado normal
            try {
                connection.setAutoCommit(true);
            } catch (SQLException finalEx) {
                throw new RuntimeException("Error al restaurar el auto-commit de la conexión", finalEx);
            }
        }
        return pedido;
    }

    @Override
    public Optional<Pedido> buscarPedidoPorId(int id) {
        // Para buscar un pedido completo, necesitamos 2 consultas: una para la cabecera y otra para los detalles.
        Optional<Pedido> pedidoOptional = buscarCabeceraPedido(id);

        if (pedidoOptional.isPresent()) {
            Pedido pedido = pedidoOptional.get();
            List<DetallePedido> detalles = buscarDetallesPorPedidoId(pedido.getIdPedido());
            pedido.setDetalles(detalles);
            return Optional.of(pedido);
        }

        return Optional.empty();
    }

    // Método de ayuda para buscar solo la cabecera de un pedido
    private Optional<Pedido> buscarCabeceraPedido(int id) {
        String sql = "SELECT p.*, u.id_usuario, u.nombre as cliente_nombre, u.correo as cliente_correo " +
                "FROM pedidos p " +
                "JOIN usuarios u ON p.id_cliente = u.id_usuario " +
                "WHERE p.id_pedido = ?";
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setInt(1, id);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapResultSetToPedido(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cabecera de pedido por ID", e);
        }
        return Optional.empty();
    }

    // Método de ayuda para buscar los detalles de un pedido
    private List<DetallePedido> buscarDetallesPorPedidoId(int idPedido) {
        List<DetallePedido> detalles = new ArrayList<>();
        String sql = "SELECT d.*, pr.nombre_producto, pr.precio as producto_precio " +
                "FROM detalle_pedido d " +
                "JOIN productos pr ON d.id_producto = pr.id_producto " +
                "WHERE d.id_pedido = ?";
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setInt(1, idPedido);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    DetallePedido detalle = new DetallePedido();
                    detalle.setIdDetallePedido(rs.getInt("id_detalle_pedido"));
                    detalle.setCantidad(rs.getInt("cantidad"));
                    detalle.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    detalle.setSubtotal(rs.getBigDecimal("subtotal"));

                    Producto producto = new Producto();
                    producto.setIdProducto(rs.getInt("id_producto"));
                    producto.setNombreProducto(rs.getString("nombre_producto"));
                    producto.setPrecio(rs.getBigDecimal("producto_precio"));

                    detalle.setProducto(producto);
                    detalles.add(detalle);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar detalles de pedido", e);
        }
        return detalles;
    }


    @Override
    public List<Pedido> buscarPedidosPorClienteId(int idCliente) {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.*, u.id_usuario, u.nombre as cliente_nombre, u.correo as cliente_correo " +
                "FROM pedidos p " +
                "JOIN usuarios u ON p.id_cliente = u.id_usuario " +
                "WHERE p.id_cliente = ?";
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setInt(1, idCliente);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    pedidos.add(mapResultSetToPedido(rs));
                }
            }
        } catch (SQLException e){
            throw new RuntimeException("Error al buscar pedidos por cliente", e);
        }
        return pedidos;
    }

    @Override
    public List<Pedido> buscarTodosLosPedidos() {
        // Similar a buscar por cliente, pero sin el WHERE.
        return new ArrayList<>(); // Implementación omitida por brevedad
    }

    // Método de ayuda para mapear un ResultSet a un objeto Pedido (sin detalles)
    private Pedido mapResultSetToPedido(ResultSet rs) throws SQLException {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(rs.getInt("id_pedido"));
        pedido.setFechaPedido(rs.getTimestamp("fecha_pedido").toLocalDateTime());
        pedido.setTotalPedido(rs.getBigDecimal("total_pedido"));
        pedido.setEstadoPedido(EstadoPedido.valueOf(rs.getString("estado_pedido")));

        Cliente cliente = new Cliente();
        cliente.setId(rs.getInt("id_cliente"));
        cliente.setNombre(rs.getString("cliente_nombre"));
        cliente.setCorreo(rs.getString("cliente_correo"));

        pedido.setCliente(cliente);

        return pedido;
    }
}