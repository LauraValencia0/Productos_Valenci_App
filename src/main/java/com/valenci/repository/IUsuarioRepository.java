package com.valenci.repository;

import com.valenci.domain.Usuario;
import java.util.List;
import java.util.Optional;

public interface IUsuarioRepository {

    void guardarUsuario(Usuario usuario);

    Optional<Usuario> buscarUsuarioPorId(int id);

    Optional<Usuario> buscarUsuarioPorCorreo(String correo);

    List<Usuario> buscarTodosLosUsuarios();

    void eliminarUsuarioPorId(int id);
}