package com.unla.museo.seguridad.jwt;

import com.unla.museo.seguridad.RoleEntity;
import com.unla.museo.seguridad.UserEntity;
import com.unla.museo.seguridad.UserNotFoundException;
import com.unla.museo.seguridad.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    private static final String SECRET = "test-secret-with-at-least-32-characters";

    @Mock private UserRepository userRepository;
    private JwtServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JwtServiceImpl(userRepository);
        ReflectionTestUtils.setField(service, "secret", SECRET);
        ReflectionTestUtils.setField(service, "accessTokenExpirationMs", 900_000L);
    }

    @Test
    void generateAccessTokenStoresSubjectAndSingleRole() {
        UserEntity user = userWithRole("ADMINISTRADOR");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        String token = service.generateAccessToken("user@test.com");
        Claims claims = service.extractAllClaims(token);

        assertEquals("user@test.com", claims.getSubject());
        assertEquals("ADMINISTRADOR", claims.get("role", String.class));
        assertEquals(1, claims.entrySet().stream()
                .filter(entry -> "role".equals(entry.getKey()))
                .count());
    }

    @Test
    void invalidSignatureIsRejectedByExtractAllClaims() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(userWithRole("CURADOR")));
        String token = service.generateAccessToken("user@test.com");
        JwtServiceImpl otherService = new JwtServiceImpl(userRepository);
        ReflectionTestUtils.setField(otherService, "secret", "other-secret-with-at-least-32-characters");

        assertThrows(SignatureException.class, () -> otherService.extractAllClaims(token));
    }

    @Test
    void expiredTokenIsRejectedByExtractAllClaims() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("user@test.com")
                .claim("role", "CURADOR")
                .issuedAt(new Date(System.currentTimeMillis() - 2_000))
                .expiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(key)
                .compact();

        assertThrows(ExpiredJwtException.class, () -> service.extractAllClaims(token));
    }

    @Test
    void unknownUserCannotReceiveToken() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.generateAccessToken("missing@test.com"));
    }

    @Test
    void userWithoutRoleCannotReceiveToken() {
        UserEntity user = new UserEntity();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> service.generateAccessToken("user@test.com"));
    }

    private UserEntity userWithRole(String id) {
        RoleEntity role = new RoleEntity();
        role.setId(id);
        UserEntity user = new UserEntity();
        user.setRole(role);
        return user;
    }
}
