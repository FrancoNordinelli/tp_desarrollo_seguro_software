package com.unla.museo.seguridad;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    // Los roles hacen falta siempre (sin ellos no se puede ni loguear un
    // usuario existente en una base ya poblada). Los datos de ejemplo, no.
    @Bean
    @Order(1)
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            crearRolSiNoExiste(roleRepository, Roles.VISITANTE, "Visitante", "Usuario visitante del museo");
            crearRolSiNoExiste(roleRepository, Roles.CURADOR, "Curador", "Usuario encargado de la gestión del museo");
            crearRolSiNoExiste(roleRepository, Roles.ADMIN, "Administrador", "Usuario administrador del sistema");
        };
    }

    // @Order(2): los eventos de ejemplo (EventosDataInitializer) necesitan que
    // estos usuarios ya existan, para asignarlos como curadores e inscriptos.
    @Bean
    @Order(2)
    @ConditionalOnProperty(name = "app.datos-de-ejemplo", havingValue = "true", matchIfMissing = true)
    CommandLineRunner initUsuariosDeEjemplo(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            crearUsuarioSiNoExiste(userRepository, roleRepository, passwordEncoder,
                    "visitante@test.com", "Carlos", "Hernandez", "1100000001", Roles.VISITANTE);
            crearUsuarioSiNoExiste(userRepository, roleRepository, passwordEncoder,
                    "curador@test.com", "Juan", "Perez", "1100000000", Roles.CURADOR);
            crearUsuarioSiNoExiste(userRepository, roleRepository, passwordEncoder,
                    "admin@test.com", "Nacho", "Medina", "1100000002", Roles.ADMIN);
            crearUsuarioSiNoExiste(userRepository, roleRepository, passwordEncoder,
                    "marta.gomez@test.com", "Marta", "Gómez", "1100000003", Roles.CURADOR);

            crearUsuarioSiNoExiste(userRepository, roleRepository, passwordEncoder,
                    "maria.gonzalez@test.com", "María", "González", "1100000004", Roles.VISITANTE);
            crearUsuarioSiNoExiste(userRepository, roleRepository, passwordEncoder,
                    "lucia.fernandez@test.com", "Lucía", "Fernández", "1100000005", Roles.VISITANTE);
            crearUsuarioSiNoExiste(userRepository, roleRepository, passwordEncoder,
                    "martin.rodriguez@test.com", "Martín", "Rodríguez", "1100000006", Roles.VISITANTE);
            crearUsuarioSiNoExiste(userRepository, roleRepository, passwordEncoder,
                    "sofia.lopez@test.com", "Sofía", "López", "1100000007", Roles.VISITANTE);
            crearUsuarioSiNoExiste(userRepository, roleRepository, passwordEncoder,
                    "nicolas.diaz@test.com", "Nicolás", "Díaz", "1100000008", Roles.VISITANTE);
            crearUsuarioSiNoExiste(userRepository, roleRepository, passwordEncoder,
                    "valentina.martinez@test.com", "Valentina", "Martínez", "1100000009", Roles.VISITANTE);
            crearUsuarioSiNoExiste(userRepository, roleRepository, passwordEncoder,
                    "federico.sanchez@test.com", "Federico", "Sánchez", "1100000010", Roles.VISITANTE);
            crearUsuarioSiNoExiste(userRepository, roleRepository, passwordEncoder,
                    "camila.romero@test.com", "Camila", "Romero", "1100000011", Roles.VISITANTE);
        };
    }

    private void crearRolSiNoExiste(
            RoleRepository roleRepository,
            String id,
            String name,
            String description) {

        if (roleRepository.findById(id).isEmpty()) {

            RoleEntity role = new RoleEntity();

            role.setId(id);
            role.setName(name);
            role.setDescription(description);

            roleRepository.save(role);
        }
    }

    private void crearUsuarioSiNoExiste(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            String email,
            String firstName,
            String lastName,
            String phoneNumber,
            String roleId) {

        if (userRepository.findByEmail(email).isEmpty()) {

            RoleEntity role = roleRepository.findById(roleId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "No existe el rol: " + roleId
                            )
                    );

            UserEntity user = new UserEntity();

            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhoneNumber(phoneNumber);
            user.setActive(true);
            user.setRole(role);
            user.setPassword(
                    passwordEncoder.encode("Aa@12345678")
            );
            user.setCreation(LocalDateTime.now());

            userRepository.save(user);
        }
    }
}
