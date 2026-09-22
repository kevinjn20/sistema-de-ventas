package com.sistema.ventas.security;

import com.sistema.ventas.entities.Usuario;
import com.sistema.ventas.repositories.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserDetailsServiceImpl service;

    @Test
    @DisplayName("loadUserByUsername mapea rol a ROLE_ADMIN")
    void mapsRole() {
        Usuario admin = Usuario.builder()
                .username("admin").password("hash").rol("ADMIN").activo(true).build();
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        UserDetails details = service.loadUserByUsername("admin");

        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("loadUserByUsername lanza excepcion si no existe o esta inactivo")
    void unknownOrInactive() {
        when(usuarioRepository.findByUsername("nadie")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserByUsername("nadie"))
                .isInstanceOf(UsernameNotFoundException.class);

        Usuario inactivo = Usuario.builder()
                .username("v").password("h").rol("VENDEDOR").activo(false).build();
        when(usuarioRepository.findByUsername("v")).thenReturn(Optional.of(inactivo));
        assertThatThrownBy(() -> service.loadUserByUsername("v"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
