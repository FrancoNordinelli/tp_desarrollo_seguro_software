package com.unla.museo.seguridad;

import com.unla.museo.seguridad.dto.UserCreateRequest;
import com.unla.museo.seguridad.dto.UserTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private UserServiceImpl service;
    private final UserMapper userMapper = new UserMapper();

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository, roleRepository, passwordEncoder, userMapper);
    }

    @Test
    void createPersistsUserWithEncodedPasswordAndRole() {
        UserCreateRequest request = validRequest();
        RoleEntity role = role("CURADOR", "Curador");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findById(anyString())).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserTO result = service.create(request);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertEquals("encoded", captor.getValue().getPassword());
        assertSame(role, captor.getValue().getRole());
        assertEquals(request.getEmail(), result.getEmail());
    }

    @Test
    void createRejectsDuplicatedEmail() {
        UserCreateRequest request = validRequest();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> service.create(request));
        verifyNoInteractions(roleRepository, passwordEncoder);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createRejectsUnknownRole() {
        UserCreateRequest request = validRequest();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.create(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createNormalizesEmailWithUppercaseAndSpaces() {
        UserCreateRequest request = validRequest();
        request.setEmail("  Ana@Test.com  ");
        RoleEntity role = role("VISITANTE", "Visitante");
        when(userRepository.existsByEmail("ana@test.com")).thenReturn(false);
        when(roleRepository.findById(anyString())).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserTO result = service.create(request);

        assertEquals("ana@test.com", result.getEmail());
        verify(userRepository).existsByEmail("ana@test.com");
    }

    @Test
    void validateCredentialsReturnsTrueForMatchingPassword() {
        UserEntity user = user("user@test.com", "encoded");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1!", "encoded")).thenReturn(true);

        assertTrue(service.validateCredentials(user.getEmail(), "Password1!"));
    }

    @Test
    void validateCredentialsNormalizesEmailWithUppercaseAndSpaces() {
        UserEntity user = user("ana@test.com", "encoded");
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1!", "encoded")).thenReturn(true);

        assertTrue(service.validateCredentials("  Ana@Test.com  ", "Password1!"));
    }

    @Test
    void validateCredentialsRejectsWrongPasswordWithGenericMessage() {
        UserEntity user = user("user@test.com", "encoded");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> service.validateCredentials(user.getEmail(), "wrong"));
        assertEquals("Email o contraseña incorrectos", ex.getMessage());
    }

    @Test
    void validateCredentialsRejectsUnknownUserWithSameGenericMessage() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> service.validateCredentials("missing@test.com", "Password1!"));
        assertEquals("Email o contraseña incorrectos", ex.getMessage());
    }

    @Test
    void getByEmailReturnsMappedUser() {
        UserEntity user = user("user@test.com", "encoded");
        user.setRole(role("CURADOR", "Curador"));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserTO result = service.getByEmail(user.getEmail());

        assertEquals(user.getEmail(), result.getEmail());
        assertEquals("Curador", result.getRole());
    }

    @Test
    void getByEmailRejectsUnknownUser() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.getByEmail("missing@test.com"));
    }

    private UserCreateRequest validRequest() {
        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("user@test.com");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setPhoneNumber("123");
        request.setPassword("Password1!");
        return request;
    }

    private UserEntity user(String email, String password) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

    private RoleEntity role(String id, String name) {
        RoleEntity role = new RoleEntity();
        role.setId(id);
        role.setName(name);
        return role;
    }
}
