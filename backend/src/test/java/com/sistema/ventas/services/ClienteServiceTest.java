package com.sistema.ventas.services;

import com.sistema.ventas.controllers.dto.ClienteRequest;
import com.sistema.ventas.controllers.dto.ClienteResponse;
import com.sistema.ventas.entities.Cliente;
import com.sistema.ventas.repositories.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    @DisplayName("listarActivos devuelve solo clientes activos")
    void listarActivos() {
        Cliente c1 = Cliente.builder().id(1L).nombre("Cliente 1").activo(true).build();
        Cliente c2 = Cliente.builder().id(2L).nombre("Cliente 2").activo(true).build();
        when(clienteRepository.findByActivoTrue()).thenReturn(List.of(c1, c2));

        List<ClienteResponse> result = clienteService.listarActivos();

        assertThat(result).hasSize(2);
        verify(clienteRepository).findByActivoTrue();
    }

    @Test
    @DisplayName("obtenerPorId lanza excepcion cuando el cliente no existe")
    void obtenerPorId_noExiste() {
        when(clienteRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtenerPorId(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("crear persiste y devuelve el cliente")
    void crear() {
        ClienteRequest request = ClienteRequest.builder()
                .nombre("Nuevo Cliente")
                .email("cliente@example.com")
                .telefono("123456789")
                .build();

        Cliente entity = Cliente.builder()
                .id(1L)
                .nombre("Nuevo Cliente")
                .email("cliente@example.com")
                .telefono("123456789")
                .activo(true)
                .build();

        when(clienteRepository.findByEmail("cliente@example.com")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenReturn(entity);

        ClienteResponse result = clienteService.crear(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Nuevo Cliente");
        assertThat(result.getEmail()).isEqualTo("cliente@example.com");
        assertThat(result.getTelefono()).isEqualTo("123456789");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("crear lanza excepcion cuando el email ya existe")
    void crear_emailDuplicado() {
        ClienteRequest request = ClienteRequest.builder()
                .nombre("Cliente")
                .email("existente@example.com")
                .build();

        when(clienteRepository.findByEmail("existente@example.com"))
                .thenReturn(Optional.of(Cliente.builder().id(1L).build()));

        assertThatThrownBy(() -> clienteService.crear(request))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("existente@example.com");
    }

    @Test
    @DisplayName("actualizar modifica y devuelve el cliente actualizado")
    void actualizar() {
        Long id = 1L;
        ClienteRequest request = ClienteRequest.builder()
                .nombre("Actualizado")
                .email("nuevo@example.com")
                .build();

        Cliente existing = Cliente.builder()
                .id(id)
                .nombre("Original")
                .email("original@example.com")
                .activo(true)
                .build();

        when(clienteRepository.findByIdAndActivoTrue(id)).thenReturn(Optional.of(existing));
        when(clienteRepository.findByEmail("nuevo@example.com")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClienteResponse result = clienteService.actualizar(id, request);

        assertThat(result.getNombre()).isEqualTo("Actualizado");
        assertThat(result.getEmail()).isEqualTo("nuevo@example.com");
    }

    @Test
    @DisplayName("eliminar hace borrado logico (activo = false)")
    void eliminar() {
        Long id = 1L;
        Cliente cliente = Cliente.builder().id(id).activo(true).build();
        when(clienteRepository.findByIdAndActivoTrue(id)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        clienteService.eliminar(id);

        assertThat(cliente.getActivo()).isFalse();
        verify(clienteRepository).save(cliente);
    }
}
