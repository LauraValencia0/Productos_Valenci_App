package com.valenci.service;

import com.valenci.domain.Usuario;
import com.valenci.repository.IUsuarioRepository;

import java.util.List;
import java.util.Optional;

public class UsuarioServiceImpl implements IUsuarioService {

    // El servicio depende de una abstracción (la interfaz), no de una implementación.
    private final IUsuarioRepository usuarioRepository;

    // La dependencia se inyecta a través del constructor.
    public UsuarioServiceImpl(IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Optional<Usuario> autenticarUsuario(String correo, String contrasena) {
        // 1. Buscar al usuario por su correo.
        Optional<Usuario> usuarioOptional = usuarioRepository.buscarUsuarioPorCorreo(correo);

        // 2. Verificar si el usuario existe.
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            // 3. Comparar la contraseña (ver nota de seguridad abajo).
            if (usuario.getContrasena().equals(contrasena)) {
                return Optional.of(usuario); // Autenticación exitosa
            }
        }

        return Optional.empty(); // Falla la autenticación si el usuario no existe o la contraseña es incorrecta.
    }

    @Override
    public void registrarUsuario(Usuario usuario) {
        // Validar que el correo no esté ya en uso.
        usuarioRepository.buscarUsuarioPorCorreo(usuario.getCorreo()).ifPresent(u -> {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        });

        // Aquí iría la lógica para encriptar la contraseña antes de guardarla.
        // Por ahora, la guardamos en texto plano.
        usuarioRepository.guardarUsuario(usuario);
    }

    @Override
    public Optional<Usuario> buscarUsuarioPorId(int id) {
        // La lógica de negocio aquí es simple: solo pasar la llamada al repositorio.
        return usuarioRepository.buscarUsuarioPorId(id);
    }

    @Override
    public List<Usuario> obtenerTodosLosUsuarios() {
        // Simplemente delegamos la llamada al repositorio.
        return usuarioRepository.buscarTodosLosUsuarios();
    }
}
