package com.sistema.ventas.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secreto-de-al-menos-32-caracteres-1234567890";

    private final JwtService jwtService = new JwtService(SECRET, 3_600_000);

    @Test
    @DisplayName("generateToken crea un token valido para el usuario")
    void generateAndValidate() {
        String token = jwtService.generateToken("vendedor", "VENDEDOR", 7L);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("vendedor");

        User details = new User("vendedor", "x",
                List.of(() -> "ROLE_VENDEDOR"));
        assertThat(jwtService.isValid(token, details)).isTrue();
    }

    @Test
    @DisplayName("isValid devuelve false para otro usuario o token manipulado")
    void invalidCases() {
        String token = jwtService.generateToken("vendedor", "VENDEDOR", 7L);

        User otro = new User("otro", "x",
                List.of(() -> "ROLE_VENDEDOR"));
        assertThat(jwtService.isValid(token, otro)).isFalse();
        assertThat(jwtService.isValid(token + "tampered", otro)).isFalse();
    }

    @Test
    @DisplayName("isValid devuelve false cuando el token expiro")
    void expiredToken() throws InterruptedException {
        JwtService corto = new JwtService(SECRET, 1);
        String token = corto.generateToken("vendedor", "VENDEDOR", 7L);
        Thread.sleep(10);

        User details = new User("vendedor", "x",
                List.of(() -> "ROLE_VENDEDOR"));
        assertThat(corto.isValid(token, details)).isFalse();
    }
}
