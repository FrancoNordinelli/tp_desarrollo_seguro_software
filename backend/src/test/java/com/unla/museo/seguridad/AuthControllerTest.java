package com.unla.museo.seguridad;

import com.unla.museo.seguridad.dto.LoginRequest;
import com.unla.museo.seguridad.dto.UserCreateRequest;
import com.unla.museo.seguridad.dto.UserTO;
import com.unla.museo.seguridad.jwt.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserServiceImpl userService;

    @Mock
    private JwtServiceImpl jwtService;

    @Mock
    private Authentication authentication;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(userService, jwtService);
    }

    @Test
    void loginReturnsTokenWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("user@test.com", "Password1!");
        when(userService.validateCredentials(request.email(), request.password())).thenReturn(true);
        when(jwtService.generateAccessToken(request.email())).thenReturn("jwt-token");

        var response = controller.login(request, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", response.getBody().accessToken());
        verify(jwtService).generateAccessToken(request.email());
    }

    @Test
    void loginPropagatesBadCredentialsWhenInvalid() {
        LoginRequest request = new LoginRequest("user@test.com", "wrong");
        doThrow(new BadCredentialsException("Email o contraseña incorrectos"))
                .when(userService).validateCredentials(request.email(), request.password());

        assertThrows(BadCredentialsException.class, () -> controller.login(request, null, null));
        verifyNoInteractions(jwtService);
    }

    @Test
    void registerCreatesUserAndReturnsSuccess() {
        UserCreateRequest request = new UserCreateRequest();
        when(userService.create(request)).thenReturn(null);

        var response = controller.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Usuario creado correctamente", response.getBody());
        verify(userService).create(request);
    }

    @Test
    void meReturnsAuthenticatedUser() {
        UserTO user = new UserTO();
        user.setEmail("user@test.com");
        when(authentication.getName()).thenReturn("user@test.com");
        when(userService.getByEmail("user@test.com")).thenReturn(user);

        var response = controller.getUserById(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(user, response.getBody());
    }
}
