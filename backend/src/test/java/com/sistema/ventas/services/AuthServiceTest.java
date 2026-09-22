package com.sistema.ventas.services;

import com.sistema.ventas.controllers.dto.LoginRequest;
import com.sistema.ventas.controllers.dto.LoginResponse;
import com.sistema.ventas.entities.Usuario;
import com.sistema.ventas.repositories.UsuarioRepository;
import com.sistema.ventas.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario() {
        return Usuario.builder()
                .id(1L).username("admin").password("hash")
                .nombre("Admin").rol("ADMIN").activo(true)
                .build();
    }

    @Test
    @DisplayName("login devuelve token Bearer cuando las credenciales son correctas")
    void loginOk() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario()));
        when(passwordEncoder.matches("admin123", "hash")).thenReturn(true);
        when(jwtService.generateToken("admin", "ADMIN", 1L)).thenReturn("jwt-token");

        LoginResponse res = authService.login(new LoginRequest("admin", "admin123"));

        assertThat(res.getToken()).isEqualTo("jwt-token");
        assertThat(res.getTokenType()).isEqualTo("Bearer");
        assertThat(res.getUsername()).isEqualTo("admin");
        assertThat(res.getRol()).isEqualTo("ADMIN");
        assertThat(res.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("login lanza 401 cuando el password es incorrecto")
    void loginBadPassword() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario()));
        when(passwordEncoder.matches("mal", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "mal")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("login lanza 401 cuando el usuario no existe o esta inactivo")
    void loginUnknownOrInactive() {
        when(usuarioRepository.findByUsername("nadie")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.login(new LoginRequest("nadie", "x")))
                .isInstanceOf(BadCredentialsException.class);

        Usuario inactivo = usuario();
        inactivo.setActivo(false);
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(inactivo));
        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "admin123")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
