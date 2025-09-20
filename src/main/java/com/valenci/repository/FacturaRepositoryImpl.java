package com.valenci.repository;

import com.valenci.domain.Factura;
import com.valenci.domain.Pedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FacturaRepositoryImpl implements IFacturaRepository {

    private final Connection connection;

    public FacturaRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void guardarFactura(Factura factura) {
        String sql = "INSERT INTO facturas (id_pedido, fecha_factura, total_factura, iva) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, factura.getPedido().getIdPedido());
            ps.setTimestamp(2, Timestamp.valueOf(factura.getFechaFactura()));
            ps.setBigDecimal(3, factura.getTotalFactura());
            ps.setBigDecimal(4, factura.getIva());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    factura.setIdFactura(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar la factura", e);
        }
    }

    @Override
    public Optional<Factura> buscarFacturaPorPedidoId(int idPedido) {
        String sql = "SELECT * FROM facturas WHERE id_pedido = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Factura factura = new Factura();
                    factura.setIdFactura(rs.getInt("id_factura"));
                    factura.setFechaFactura(rs.getTimestamp("fecha_factura").toLocalDateTime());
                    factura.setTotalFactura(rs.getBigDecimal("total_factura"));
                    factura.setIva(rs.getBigDecimal("iva"));
                    return Optional.of(factura);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar factura por ID de pedido", e);
        }
        return Optional.empty();
    }


    @Override
    public Optional<Factura> buscarFacturaPorId(int idFactura) {
        String sql = "SELECT f.*, p.id_pedido FROM facturas f JOIN pedidos p ON f.id_pedido = p.id_pedido WHERE f.id_factura = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFactura);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToFactura(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar factura por ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Factura> buscarTodasLasFacturas() {
        List<Factura> facturas = new ArrayList<>();
        String sql = "SELECT f.*, p.id_pedido FROM facturas f JOIN pedidos p ON f.id_pedido = p.id_pedido ORDER BY f.fecha_factura DESC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                facturas.add(mapResultSetToFactura(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar todas las facturas", e);
        }
        return facturas;
    }

    @Override
    public List<Factura> buscarFacturasPorClienteId(int idCliente) {
        List<Factura> facturas = new ArrayList<>();
        String sql = "SELECT f.*, p.id_pedido FROM facturas f JOIN pedidos p ON f.id_pedido = p.id_pedido WHERE p.id_cliente = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    facturas.add(mapResultSetToFactura(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar facturas por cliente", e);
        }
        return facturas;
    }

    // --- MÉTODO DE AYUDA NUEVO O MODIFICADO ---
// Para no repetir código, creamos un helper que mapee el ResultSet a un objeto Factura
    private Factura mapResultSetToFactura(ResultSet rs) throws SQLException {
        Factura factura = new Factura();
        factura.setIdFactura(rs.getInt("id_factura"));
        factura.setFechaFactura(rs.getTimestamp("fecha_factura").toLocalDateTime());
        factura.setTotalFactura(rs.getBigDecimal("total_factura"));
        factura.setIva(rs.getBigDecimal("iva"));
        Pedido pedidoParcial = new Pedido();
        pedidoParcial.setIdPedido(rs.getInt("id_pedido"));
        factura.setPedido(pedidoParcial);

        return factura;
    }
}
