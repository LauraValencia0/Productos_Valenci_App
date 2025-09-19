package com.valenci.repository;

import com.valenci.domain.*;

import java.sql.*;
import java.util.*;

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
            connection.setAutoCommit(false);
            try (PreparedStatement psPedido = connection.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                psPedido.setTimestamp(1, Timestamp.valueOf(pedido.getFechaPedido()));
                psPedido.setBigDecimal(2, pedido.getTotalPedido());
                psPedido.setString(3, pedido.getEstadoPedido().name());
                psPedido.setInt(4, pedido.getCliente().getId());
                psPedido.executeUpdate();
                try (ResultSet rs = psPedido.getGeneratedKeys()) {
                    if (rs.next()) {
                        pedido.setIdPedido(rs.getInt(1));
                    } else {
                        throw new SQLException("No se pudo obtener el ID del pedido creado.");
                    }
                }
            }
            try (PreparedStatement psDetalle = connection.prepareStatement(sqlDetalle)) {
                for (DetallePedido detalle : pedido.getDetalles()) {
                    psDetalle.setInt(1, pedido.getIdPedido());
                    psDetalle.setInt(2, detalle.getProducto().getIdProducto());
                    psDetalle.setInt(3, detalle.getCantidad());
                    psDetalle.setBigDecimal(4, detalle.getPrecioUnitario());
                    psDetalle.setBigDecimal(5, detalle.getSubtotal());
                    psDetalle.addBatch();
                }
                psDetalle.executeBatch();
            }
            connection.commit();

        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException rollbackEx) { throw new RuntimeException("Error crítico al intentar hacer rollback", rollbackEx); }
            throw new RuntimeException("Error al guardar el pedido", e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException finalEx) { throw new RuntimeException("Error al restaurar el auto-commit", finalEx); }
        }
        return pedido;
    }

    @Override
    public void actualizarPedido(Pedido pedido) {
        String sql = "UPDATE pedidos SET estado_pedido = ? WHERE id_pedido = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, pedido.getEstadoPedido().name());
            ps.setInt(2, pedido.getIdPedido());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el pedido con ID " + pedido.getIdPedido(), e);
        }
    }

    @Override
    public Optional<Pedido> buscarPedidoPorId(int id) {
        Optional<Pedido> pedidoOptional = buscarCabeceraPedido(id);
        if (pedidoOptional.isPresent()) {
            Pedido pedido = pedidoOptional.get();
            List<DetallePedido> detalles = buscarDetallesPorPedidoId(pedido.getIdPedido());
            pedido.setDetalles(detalles);
            return Optional.of(pedido);
        }
        return Optional.empty();
    }

    private Optional<Pedido> buscarCabeceraPedido(int id) {
        String sql = "SELECT p.*, u.id_usuario, u.nombre as cliente_nombre, u.correo as cliente_correo " +
                "FROM pedidos p JOIN usuarios u ON p.id_cliente = u.id_usuario WHERE p.id_pedido = ?";
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setInt(1, id);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){ return Optional.of(mapResultSetToPedido(rs)); }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cabecera de pedido por ID", e);
        }
        return Optional.empty();
    }

    private List<DetallePedido> buscarDetallesPorPedidoId(int idPedido) {
        List<DetallePedido> detalles = new ArrayList<>();
        String sql = "SELECT d.*, pr.nombre_producto, pr.precio as producto_precio " +
                "FROM detalle_pedido d JOIN productos pr ON d.id_producto = pr.id_producto WHERE d.id_pedido = ?";
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
                "FROM pedidos p JOIN usuarios u ON p.id_cliente = u.id_usuario WHERE p.id_cliente = ?";
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setInt(1, idCliente);
            try(ResultSet rs = ps.executeQuery()){ while(rs.next()){ pedidos.add(mapResultSetToPedido(rs)); } }
        } catch (SQLException e){
            throw new RuntimeException("Error al buscar pedidos por cliente", e);
        }
        return pedidos;
    }


    @Override
    public List<Pedido> buscarTodosLosPedidos() {
        List<Pedido> pedidos = new ArrayList<>();
        String sqlPedidos = "SELECT p.*, u.nombre as cliente_nombre, u.correo as cliente_correo " +
                "FROM pedidos p " +
                "JOIN usuarios u ON p.id_cliente = u.id_usuario " +
                "ORDER BY p.fecha_pedido DESC";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sqlPedidos)) {
            while (rs.next()) {
                pedidos.add(mapResultSetToPedido(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar todos los pedidos", e);
        }

        if (pedidos.isEmpty()) {
            return pedidos;
        }

        Map<Integer, List<DetallePedido>> detallesPorPedidoId = new HashMap<>();

        String placeholders = String.join(",", Collections.nCopies(pedidos.size(), "?"));
        String sqlDetalles = "SELECT d.*, pr.nombre_producto, pr.precio as producto_precio " +
                "FROM detalle_pedido d " +
                "JOIN productos pr ON d.id_producto = pr.id_producto " +
                "WHERE d.id_pedido IN (" + placeholders + ")";

        try (PreparedStatement ps = connection.prepareStatement(sqlDetalles)) {
            for (int i = 0; i < pedidos.size(); i++) {
                ps.setInt(i + 1, pedidos.get(i).getIdPedido());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idPedido = rs.getInt("id_pedido");
                    DetallePedido detalle = mapResultSetToDetallePedido(rs);
                    detallesPorPedidoId.computeIfAbsent(idPedido, k -> new ArrayList<>()).add(detalle);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar los detalles de los pedidos", e);
        }

        for (Pedido pedido : pedidos) {
            pedido.setDetalles(detallesPorPedidoId.getOrDefault(pedido.getIdPedido(), new ArrayList<>()));
        }

        return pedidos;
    }

    private DetallePedido mapResultSetToDetallePedido(ResultSet rs) throws SQLException {
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
        return detalle;
    }

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


    @Override
    public List<Pedido> buscarPedidosPorEstado(EstadoPedido estado) {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.*, u.nombre as cliente_nombre, u.correo as cliente_correo " +
                "FROM pedidos p JOIN usuarios u ON p.id_cliente = u.id_usuario " +
                "WHERE p.estado_pedido = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(mapResultSetToPedido(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pedidos por estado", e);
        }
        return pedidos;
    }

    @Override
    public List<Pedido> buscarPedidosPorFecha(java.time.LocalDate fecha) {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.*, u.nombre as cliente_nombre, u.correo as cliente_correo " +
                "FROM pedidos p JOIN usuarios u ON p.id_cliente = u.id_usuario " +
                "WHERE DATE(p.fecha_pedido) = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(mapResultSetToPedido(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pedidos por fecha", e);
        }
        return pedidos;
    }

    @Override
    public List<Pedido> buscarPedidosPorProducto(int idProducto) {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT DISTINCT p.*, u.nombre as cliente_nombre, u.correo as cliente_correo " +
                "FROM pedidos p " +
                "JOIN detalle_pedido d ON p.id_pedido = d.id_pedido " +
                "JOIN usuarios u ON p.id_cliente = u.id_usuario " +
                "WHERE d.id_producto = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(mapResultSetToPedido(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pedidos por producto", e);
        }
        return pedidos;
    }

}