package com.unla.museo.seguridad;

import org.springframework.security.core.Authentication;

/**
 * Puente entre el módulo de eventos y los usuarios: eventos nunca toca
 * UserRepository directamente, pasa siempre por acá.
 */
public interface UsuarioActualService {

    /** El usuario del token (email = Authentication.getName()). */
    UserEntity obtenerPorEmail(String email);

    /** Si el usuario autenticado es CURADOR o ADMINISTRADOR. */
    boolean esGestor(Authentication authentication);

    /** Valida que exista y tenga rol CURADOR; si no, SolicitudInvalidaException (400). */
    UserEntity obtenerCuradorValido(Long curadorId);
}
