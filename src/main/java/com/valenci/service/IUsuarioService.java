package com.valenci.service;

import com.valenci.domain.Usuario;
import java.util.List;
import java.util.Optional;

public interface IUsuarioService {

    Optional<Usuario> autenticarUsuario(String correo, String contrasena);

    void registrarUsuario(Usuario usuario);

    Optional<Usuario> buscarUsuarioPorId(int id);

    List<Usuario> obtenerTodosLosUsuarios();

    Optional<Usuario> buscarUsuarioPorCorreo(String correo);
}