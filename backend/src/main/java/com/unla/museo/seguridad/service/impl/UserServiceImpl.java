package com.unla.museo.seguridad.service.impl;


import com.unla.museo.seguridad.dto.CrearUsuarioRequest;
import com.unla.museo.seguridad.dto.PersonaTO;
import com.unla.museo.seguridad.dto.UsuarioTO;
import com.unla.museo.seguridad.dto.mapper.UsuarioMapper;
import com.unla.museo.seguridad.entity.RolEntity;
import com.unla.museo.seguridad.entity.UsuarioEntity;
import com.unla.museo.seguridad.exception.ErrorMessage;
import com.unla.museo.seguridad.exception.RecursoInexistenteException;
import com.unla.museo.seguridad.exception.UsuarioExistenteException;
import com.unla.museo.seguridad.exception.UsuarioNoEncontradoException;
import com.unla.museo.seguridad.repository.RolRepository;
import com.unla.museo.seguridad.repository.UsuarioRepository;
import com.unla.museo.seguridad.service.UserService;
import com.unla.museo.seguridad.util.Roles;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Qualifier("UserServiceImpl")
public class UserServiceImpl implements UserService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;
    public UserServiceImpl(
            @Qualifier("UsuarioSQLRepositoryImpl") UsuarioRepository usuarioRepository,
            @Qualifier("RolSQLRepositoryImpl") RolRepository rolRepository,
           PasswordEncoder passwordEncoder, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    public UsuarioTO create(CrearUsuarioRequest request) {
        // Normalizado acá (trim + minúsculas) para que el mismo email con
        // mayúsculas o espacios no cree una cuenta duplicada.
        request.setEmail(normalizarEmail(request.getEmail()));
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new UsuarioExistenteException(ErrorMessage.User.CONFLICT_EMAIL);
        }
        RolEntity rolEntity = rolRepository.findById(Roles.VISITANTE)
                .orElseThrow(() -> new RecursoInexistenteException(ErrorMessage.User.Role.NOT_FOUND));
        UsuarioEntity user = usuarioMapper.toEntity(request, rolEntity,request.getEmail());
        user.setPassword(this.passwordEncoder.encode(request.getPassword()));
        user.setCreacion(LocalDateTime.now());
        user = usuarioRepository.save(user);
        return usuarioMapper.toResponse(user);
    }


    @Override
    public boolean validateCredentials(String email, String rawPassword) {
        String emailNormalizado = normalizarEmail(email);
        // Mismo mensaje genérico si el email no existe o si la contraseña no
        // coincide: no hay que revelar si el email está registrado.
        boolean credencialesValidas = usuarioRepository.findByEmail(emailNormalizado)
                .map(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
                .orElse(false);
        if (!credencialesValidas) {
            throw new BadCredentialsException(ErrorMessage.AUTH_FAILED);
        }
        return true;
    }

    private static String normalizarEmail(String email) {
        return email == null ? null : email.strip().toLowerCase();
    }

    public UsuarioTO getByEmail(String userEmail) {
        UsuarioEntity user = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsuarioNoEncontradoException(ErrorMessage.User.NOT_FOUND));
        return usuarioMapper.toResponse(user);
    }

    @Override
    public List<PersonaTO> getCuradores() {
        return usuarioRepository.findByRoleIdOrderByFirstNameAscLastNameAsc(Roles.CURADOR).stream()
                .map(usuario -> new PersonaTO(usuario.getId(), usuario.getNombre() + " " + usuario.getApellido()))
                .toList();
    }

}
