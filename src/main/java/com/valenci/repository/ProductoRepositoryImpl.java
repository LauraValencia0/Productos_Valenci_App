package com.valenci.repository;

import com.valenci.domain.Producto;
import com.valenci.domain.Proveedor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoRepositoryImpl implements IProductoRepository {

    private final Connection connection;

    public ProductoRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void guardarProducto(Producto producto) {
        // Decide si es un INSERT (nuevo) o un UPDATE (existente)
        if (producto.getIdProducto() == 0) {
            String sql = "INSERT INTO productos (nombre_producto, descripcion, precio, cantidad, id_proveedor) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, producto.getNombreProducto());
                ps.setString(2, producto.getDescripcion());
                ps.setBigDecimal(3, producto.getPrecio());
                ps.setInt(4, producto.getCantidad());
                // Asigna el ID del proveedor, manejando el caso de que sea nulo
                if (producto.getProveedor() != null) {
                    ps.setInt(5, producto.getProveedor().getId());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()){
                    if(rs.next()){
                        producto.setIdProducto(rs.getInt(1));
                    }
                }

            } catch (SQLException e) {
                throw new RuntimeException("Error al guardar el nuevo producto", e);
            }
        } else {
            String sql = "UPDATE productos SET nombre_producto = ?, descripcion = ?, precio = ?, cantidad = ?, id_proveedor = ? WHERE id_producto = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, producto.getNombreProducto());
                ps.setString(2, producto.getDescripcion());
                ps.setBigDecimal(3, producto.getPrecio());
                ps.setInt(4, producto.getCantidad());
                if (producto.getProveedor() != null) {
                    ps.setInt(5, producto.getProveedor().getId());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }
                ps.setInt(6, producto.getIdProducto());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error al actualizar el producto", e);
            }
        }
    }

    @Override
    public Optional<Producto> buscarProductoPorId(int id) {
        // Usamos un JOIN para traer los datos del producto y su proveedor en una sola consulta.
        String sql = "SELECT p.*, u.id_usuario, u.nombre as proveedor_nombre, u.correo as proveedor_correo, u.nombre_empresa " +
                "FROM productos p " +
                "LEFT JOIN usuarios u ON p.id_proveedor = u.id_usuario " +
                "WHERE p.id_producto = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProducto(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producto por ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Producto> buscarTodosLosProductos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.*, u.id_usuario, u.nombre as proveedor_nombre, u.correo as proveedor_correo, u.nombre_empresa " +
                "FROM productos p " +
                "LEFT JOIN usuarios u ON p.id_proveedor = u.id_usuario";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                productos.add(mapResultSetToProducto(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar todos los productos", e);
        }
        return productos;
    }

    @Override
    public void eliminarProductoPorId(int id) {
        String sql = "DELETE FROM productos WHERE id_producto = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el producto", e);
        }
    }

    /**
     * Método de ayuda para convertir una fila del ResultSet (que incluye datos del JOIN)
     * en un objeto Producto con su respectivo objeto Proveedor anidado.
     */
    private Producto mapResultSetToProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(rs.getInt("id_producto"));
        producto.setNombreProducto(rs.getString("nombre_producto"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setPrecio(rs.getBigDecimal("precio"));
        producto.setCantidad(rs.getInt("cantidad"));

        // Mapear el proveedor si existe (resultado del LEFT JOIN)
        int idProveedor = rs.getInt("id_proveedor");
        if (!rs.wasNull()) {
            Proveedor proveedor = new Proveedor();
            proveedor.setId(idProveedor);
            // Usamos los alias definidos en la consulta SQL para evitar ambigüedad de columnas
            proveedor.setNombre(rs.getString("proveedor_nombre"));
            proveedor.setCorreo(rs.getString("proveedor_correo"));
            proveedor.setNombreEmpresa(rs.getString("nombre_empresa"));
            producto.setProveedor(proveedor);
        }
        return producto;
    }
}
