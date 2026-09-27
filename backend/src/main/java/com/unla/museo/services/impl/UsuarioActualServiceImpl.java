package com.unla.museo.services.impl;

import com.unla.museo.comun.errores.SolicitudInvalidaException;
import com.unla.museo.constants.Roles;
import com.unla.museo.entities.UserEntity;
import com.unla.museo.exception.ErrorMessage;
import com.unla.museo.exception.UserNotFoundException;
import com.unla.museo.repositories.UserRepository;
import com.unla.museo.services.UsuarioActualService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class UsuarioActualServiceImpl implements UsuarioActualService {

    private final UserRepository userRepository;

    public UsuarioActualServiceImpl(@Qualifier("UserSQLRepositoryImpl") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserEntity obtenerPorEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessage.User.NOT_FOUND));
    }

    @Override
    public boolean esGestor(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(autoridad -> autoridad.equals("ROLE_" + Roles.CURADOR) || autoridad.equals("ROLE_" + Roles.ADMIN));
    }

    @Override
    public UserEntity obtenerCuradorValido(Long curadorId) {
        if (curadorId == null) {
            throw new SolicitudInvalidaException("Debe indicarse un curador responsable");
        }
        UserEntity usuario = userRepository.findById(curadorId)
                .orElseThrow(() -> new SolicitudInvalidaException("El curador indicado no existe"));
        if (usuario.getRole() == null || !Roles.CURADOR.equals(usuario.getRole().getId())) {
            throw new SolicitudInvalidaException("El usuario indicado no tiene rol de curador");
        }
        return usuario;
    }
}
