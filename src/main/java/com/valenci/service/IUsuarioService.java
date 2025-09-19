package com.valenci.service;

import com.valenci.domain.Usuario;
import java.util.List;
import java.util.Optional;

public interface IUsuarioService {

    void registrarUsuario(Usuario usuario);

    void actualizarUsuario(Usuario usuario);

    Optional<Usuario> autenticarUsuario(String correo, String contrasena);

    Optional<Usuario> buscarUsuarioPorId(int id);

    Optional<Usuario> buscarUsuarioPorCorreo(String correo);

    List<Usuario> obtenerTodosLosUsuarios();

}