package com.sistema.ventas.services;

import com.sistema.ventas.controllers.dto.UsuarioRequest;
import com.sistema.ventas.controllers.dto.UsuarioResponse;
import com.sistema.ventas.entities.Usuario;
import com.sistema.ventas.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("listarActivos devuelve solo usuarios activos")
    void listarActivos() {
        Usuario u1 = Usuario.builder().id(1L).username("admin").nombre("Admin").rol("ADMIN").activo(true).build();
        Usuario u2 = Usuario.builder().id(2L).username("vendedor").nombre("Vendedor").rol("VENDEDOR").activo(true).build();
        when(usuarioRepository.findByActivoTrue()).thenReturn(List.of(u1, u2));

        List<UsuarioResponse> result = usuarioService.listarActivos();

        assertThat(result).hasSize(2);
        verify(usuarioRepository).findByActivoTrue();
    }

    @Test
    @DisplayName("obtenerPorId lanza excepcion cuando el usuario no existe")
    void obtenerPorId_noExiste() {
        when(usuarioRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.obtenerPorId(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("crear persiste y devuelve el usuario con password hasheado")
    void crear() {
        UsuarioRequest request = UsuarioRequest.builder()
                .username("nuevo")
                .password("password123")
                .nombre("Nuevo Usuario")
                .rol("VENDEDOR")
                .build();

        when(usuarioRepository.findByUsername("nuevo")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("$2a$hashed");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        UsuarioResponse result = usuarioService.crear(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("nuevo");
        assertThat(result.getNombre()).isEqualTo("Nuevo Usuario");
        assertThat(result.getRol()).isEqualTo("VENDEDOR");
        verify(passwordEncoder).encode("password123");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("crear lanza excepcion cuando el username ya existe")
    void crear_usernameDuplicado() {
        UsuarioRequest request = UsuarioRequest.builder()
                .username("existente")
                .password("password123")
                .build();

        when(usuarioRepository.findByUsername("existente"))
                .thenReturn(Optional.of(Usuario.builder().id(1L).build()));

        assertThatThrownBy(() -> usuarioService.crear(request))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("existente");
    }

    @Test
    @DisplayName("actualizar modifica y devuelve el usuario actualizado")
    void actualizar() {
        Long id = 1L;
        UsuarioRequest request = UsuarioRequest.builder()
                .username("actualizado")
                .password("nuevaPass")
                .nombre("Actualizado")
                .rol("ADMIN")
                .build();

        Usuario existing = Usuario.builder()
                .id(id)
                .username("original")
                .password("hashOriginal")
                .nombre("Original")
                .rol("VENDEDOR")
                .activo(true)
                .build();

        when(usuarioRepository.findByIdAndActivoTrue(id)).thenReturn(Optional.of(existing));
        when(usuarioRepository.findByUsername("actualizado")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("nuevaPass")).thenReturn("$2a$nuevoHash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponse result = usuarioService.actualizar(id, request);

        assertThat(result.getUsername()).isEqualTo("actualizado");
        assertThat(result.getNombre()).isEqualTo("Actualizado");
        assertThat(result.getRol()).isEqualTo("ADMIN");
        verify(passwordEncoder).encode("nuevaPass");
    }

    @Test
    @DisplayName("eliminar hace borrado logico (activo = false)")
    void eliminar() {
        Long id = 1L;
        Usuario usuario = Usuario.builder().id(id).activo(true).build();
        when(usuarioRepository.findByIdAndActivoTrue(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        usuarioService.eliminar(id);

        assertThat(usuario.getActivo()).isFalse();
        verify(usuarioRepository).save(usuario);
    }
}
