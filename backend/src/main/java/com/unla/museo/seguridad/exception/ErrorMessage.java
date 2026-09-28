package com.unla.museo.seguridad.exception;

public interface ErrorMessage {

    String INTERNAL_ERROR = "Ocurrió un error inesperado. Por favor, contacte al equipo de soporte";
    // Mismo mensaje para email inexistente y contraseña incorrecta: no hay
    // que revelar si el email está registrado.
    String AUTH_FAILED = "Email o contraseña incorrectos";
    interface User {
        String NOT_FOUND = "Usuario no encontrado";
        String CONFLICT_EMAIL = "El email ya esta registrado.";

        interface Role {
            String NOT_FOUND = "El rol no encontrado";
        }
    }
}
