package com.valenci.service;

import com.valenci.domain.Usuario;
import com.valenci.repository.IUsuarioRepository;

import java.util.List;
import java.util.Optional;

public class UsuarioServiceImpl implements IUsuarioService {

    private final IUsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Optional<Usuario> autenticarUsuario(String correo, String contrasena) {
        Optional<Usuario> usuarioOptional = usuarioRepository.buscarUsuarioPorCorreo(correo);
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            if (usuario.getContrasena().equals(contrasena)) {
                return Optional.of(usuario);
            }
        }
        return Optional.empty();
    }

    @Override
    public void registrarUsuario(Usuario usuario) {
        usuarioRepository.buscarUsuarioPorCorreo(usuario.getCorreo()).ifPresent(u -> {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        });
        usuarioRepository.guardarUsuario(usuario);
    }

    @Override
    public Optional<Usuario> buscarUsuarioPorId(int id) {
        return usuarioRepository.buscarUsuarioPorId(id);
    }

    @Override
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.buscarTodosLosUsuarios();
    }

    @Override
    public Optional<Usuario> buscarUsuarioPorCorreo(String correo) {
        return usuarioRepository.buscarUsuarioPorCorreo(correo);
    }
}