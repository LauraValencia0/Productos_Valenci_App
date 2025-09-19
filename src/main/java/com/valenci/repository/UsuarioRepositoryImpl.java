package com.valenci.repository;

import com.valenci.domain.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepositoryImpl implements IUsuarioRepository {

    private final Connection connection;

    public UsuarioRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void guardarUsuario(Usuario usuario) {
        if (usuario.getId() == 0) {
            String sql = "INSERT INTO usuarios (nombre, correo, contrasena, telefono, rol, direccion_envio, cargo, salario, nombre_empresa) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, usuario.getNombre());
                ps.setString(2, usuario.getCorreo());
                ps.setString(3, usuario.getContrasena());
                ps.setString(4, null);

                if (usuario instanceof Cliente) {
                    ps.setString(5, "CLIENTE");
                    ps.setString(6, ((Cliente) usuario).getDireccionEnvio());
                    ps.setNull(7, Types.VARCHAR);
                    ps.setNull(8, Types.DECIMAL);
                    ps.setNull(9, Types.VARCHAR);
                } else if (usuario instanceof Empleado) {
                    ps.setString(5, "EMPLEADO");
                    ps.setNull(6, Types.VARCHAR);
                    ps.setString(7, ((Empleado) usuario).getCargo());
                    ps.setBigDecimal(8, ((Empleado) usuario).getSalario());
                    ps.setNull(9, Types.VARCHAR);
                } else if (usuario instanceof Proveedor) {
                    ps.setString(5, "PROVEEDOR");
                    ps.setNull(6, Types.VARCHAR);
                    ps.setNull(7, Types.VARCHAR);
                    ps.setNull(8, Types.DECIMAL);
                    ps.setString(9, ((Proveedor) usuario).getNombreEmpresa());
                } else if (usuario instanceof Administrador) {
                    ps.setString(5, "ADMINISTRADOR");
                    ps.setNull(6, Types.VARCHAR);
                    ps.setNull(7, Types.VARCHAR);
                    ps.setNull(8, Types.DECIMAL);
                    ps.setNull(9, Types.VARCHAR);
                }

                ps.executeUpdate();

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        usuario.setId(generatedKeys.getInt(1));
                    }
                }

            } catch (SQLException e) {
                throw new RuntimeException("Error al guardar el usuario", e);
            }
        } else {

        }
    }

    @Override
    public Optional<Usuario> buscarUsuarioPorId(int id) {
        String sql = "SELECT * FROM usuarios WHERE id_usuario = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario por ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> buscarUsuarioPorCorreo(String correo) {
        String sql = "SELECT * FROM usuarios WHERE correo = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario por correo", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Usuario> buscarTodosLosUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar todos los usuarios", e);
        }
        return usuarios;
    }

    @Override
    public void eliminarUsuarioPorId(int id) {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar usuario", e);
        }
    }

    /**
     * Método de ayuda para convertir una fila del ResultSet en el objeto Usuario correcto.
     * Esta es la clave para manejar la herencia.
     */
    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_usuario");
        String nombre = rs.getString("nombre");
        String correo = rs.getString("correo");
        String contrasena = rs.getString("contrasena");
        String rol = rs.getString("rol");

        Usuario usuario = null;

        switch (rol) {
            case "CLIENTE":
                Cliente cliente = new Cliente();
                cliente.setDireccionEnvio(rs.getString("direccion_envio"));
                usuario = cliente;
                break;
            case "EMPLEADO":
                Empleado empleado = new Empleado();
                empleado.setCargo(rs.getString("cargo"));
                empleado.setSalario(rs.getBigDecimal("salario"));
                usuario = empleado;
                break;
            case "PROVEEDOR":
                Proveedor proveedor = new Proveedor();
                proveedor.setNombreEmpresa(rs.getString("nombre_empresa"));
                usuario = proveedor;
                break;
            case "ADMINISTRADOR":
                usuario = new Administrador();
                break;
            default:
                throw new SQLException("Rol de usuario desconocido: " + rol);
        }

        usuario.setId(id);
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setContrasena(contrasena);

        return usuario;
    }
}
