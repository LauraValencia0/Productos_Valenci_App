package com.valenci.repository;

import com.valenci.domain.MetodoPago;
import com.valenci.domain.Pago;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagoRepositoryImpl implements IPagoRepository {

    private final Connection connection;

    public PagoRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void guardarPago(Pago pago) {
        String sql = "INSERT INTO pagos (id_pedido, monto, fecha_pago, metodo_pago) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pago.getPedido().getIdPedido());
            ps.setBigDecimal(2, pago.getMonto());
            ps.setTimestamp(3, Timestamp.valueOf(pago.getFechaPago()));
            ps.setString(4, pago.getMetodoPago().name());
            ps.executeUpdate();

            try(ResultSet rs = ps.getGeneratedKeys()){
                if(rs.next()){
                    pago.setIdPago(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el pago", e);
        }
    }

    @Override
    public List<Pago> buscarPagosPorPedidoId(int idPedido) {
        List<Pago> pagos = new ArrayList<>();
        String sql = "SELECT * FROM pagos WHERE id_pedido = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    Pago pago = new Pago();
                    pago.setIdPago(rs.getInt("id_pago"));
                    pago.setMonto(rs.getBigDecimal("monto"));
                    pago.setFechaPago(rs.getTimestamp("fecha_pago").toLocalDateTime());
                    pago.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
                    pagos.add(pago);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pagos por ID de pedido", e);
        }
        return pagos;
    }
}