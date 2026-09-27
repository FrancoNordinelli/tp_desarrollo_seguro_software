package com.unla.museo.seguridad;


import com.unla.museo.seguridad.dto.UserCreateRequest;
import com.unla.museo.seguridad.dto.PersonaTO;
import com.unla.museo.seguridad.dto.UserTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Qualifier("UserServiceImpl")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    public UserServiceImpl(
            @Qualifier("UserSQLRepositoryImpl") UserRepository userRepository,
            @Qualifier("RoleSQLRepositoryImpl") RoleRepository roleRepository,
           PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public UserTO create(UserCreateRequest request) {
        // Normalizado acá (trim + minúsculas) para que el mismo email con
        // mayúsculas o espacios no cree una cuenta duplicada.
        request.setEmail(normalizarEmail(request.getEmail()));
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(ErrorMessage.User.CONFLICT_EMAIL);
        }
        RoleEntity roleEntity = roleRepository.findById(Roles.VISITANTE)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.User.Role.NOT_FOUND));
        UserEntity user = userMapper.toEntity(request,roleEntity,request.getEmail());
        user.setPassword(this.passwordEncoder.encode(request.getPassword()));
        user.setCreation(LocalDateTime.now());
        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }


    @Override
    public boolean validateCredentials(String email, String rawPassword) {
        String emailNormalizado = normalizarEmail(email);
        // Mismo mensaje genérico si el email no existe o si la contraseña no
        // coincide: no hay que revelar si el email está registrado.
        boolean credencialesValidas = userRepository.findByEmail(emailNormalizado)
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

    public UserTO getByEmail(String userEmail) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessage.User.NOT_FOUND));
        return userMapper.toResponse(user);
    }

    @Override
    public List<PersonaTO> getCuradores() {
        return userRepository.findByRoleIdOrderByFirstNameAscLastNameAsc(Roles.CURADOR).stream()
                .map(usuario -> new PersonaTO(usuario.getId(), usuario.getFirstName() + " " + usuario.getLastName()))
                .toList();
    }

}
