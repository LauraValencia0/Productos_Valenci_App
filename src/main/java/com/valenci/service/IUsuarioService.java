package com.valenci.service;

import com.valenci.domain.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define el contrato para la lógica de negocio
 * relacionada con los usuarios del sistema.
 */
public interface IUsuarioService {

    /**
     * Autentica a un usuario basado en sus credenciales.
     * @param correo El correo electrónico del usuario.
     * @param contrasena La contraseña sin encriptar del usuario.
     * @return Un Optional con el objeto Usuario si la autenticación es exitosa,
     * de lo contrario un Optional vacío.
     */
    Optional<Usuario> autenticarUsuario(String correo, String contrasena);

    /**
     * Registra un nuevo usuario en el sistema.
     * La implementación de este método se encargará de validar
     * que el correo no exista y de encriptar la contraseña antes de guardarla.
     * @param usuario El nuevo usuario a registrar.
     */
    void registrarUsuario(Usuario usuario);

    /**
     * Busca un usuario por su ID.
     * @param id El ID del usuario.
     * @return Un Optional con el usuario si se encuentra.
     */
    Optional<Usuario> buscarUsuarioPorId(int id);

    /**
     * Obtiene una lista de todos los usuarios registrados.
     * @return Una lista de objetos Usuario.
     */
    List<Usuario> obtenerTodosLosUsuarios();
}