package com.valenci.repository;

import com.valenci.domain.Factura;
import com.valenci.domain.Pedido;

import java.sql.*;
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
                    // En una implementación completa, aquí también buscaríamos el Pedido asociado.
                    // Por simplicidad, asumimos que el servicio se encargará de ensamblar el objeto completo.
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
}
