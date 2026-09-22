package com.sistema.ventas.services;

import com.sistema.ventas.controllers.dto.VentaRequest;
import com.sistema.ventas.controllers.dto.VentaResponse;
import com.sistema.ventas.entities.*;
import com.sistema.ventas.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private VentaService ventaService;

    @Test
    @DisplayName("listarActivos devuelve solo ventas activas ordenadas por fecha descendente")
    void listarActivos() {
        Cliente cliente = Cliente.builder().id(1L).nombre("Cliente").build();
        Usuario usuario = Usuario.builder().id(1L).username("vendedor").build();
        Venta v = Venta.builder()
                .id(1L).fecha(LocalDateTime.now()).total(BigDecimal.TEN)
                .cliente(cliente).usuario(usuario)
                .detalles(List.of()).activo(true).build();
        when(ventaRepository.findByActivoTrueOrderByCreatedAtDesc()).thenReturn(List.of(v));

        List<VentaResponse> result = ventaService.listarActivos();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(ventaRepository).findByActivoTrueOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("obtenerPorId lanza excepcion cuando la venta no existe")
    void obtenerPorId_noExiste() {
        when(ventaRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.obtenerPorId(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("crear persiste la venta, descuenta stock y devuelve la respuesta")
    void crear_exitoso() {
        Cliente cliente = Cliente.builder().id(1L).nombre("Cliente Test").build();
        Usuario usuario = Usuario.builder().id(1L).username("vendedor").build();
        Producto producto = Producto.builder()
                .id(1L).nombre("Producto Test").precio(new BigDecimal("100"))
                .stock(10).activo(true).build();

        when(clienteRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(producto));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> {
            Venta v = invocation.getArgument(0);
            v.setId(1L);
            v.getDetalles().forEach(d -> d.setId(10L));
            return v;
        });
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VentaRequest request = VentaRequest.builder()
                .clienteId(1L)
                .usuarioId(1L)
                .items(List.of(
                        VentaRequest.DetalleRequest.builder().productoId(1L).cantidad(3).build()
                ))
                .build();

        VentaResponse result = ventaService.crear(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("300"));
        assertThat(result.getDetalles()).hasSize(1);
        assertThat(result.getDetalles().get(0).getSubtotal()).isEqualByComparingTo(new BigDecimal("300"));
        assertThat(producto.getStock()).isEqualTo(7);
        verify(ventaRepository).save(any(Venta.class));
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    @DisplayName("crear lanza excepcion cuando el stock es insuficiente")
    void crear_stockInsuficiente() {
        Cliente cliente = Cliente.builder().id(1L).nombre("Cliente Test").build();
        Usuario usuario = Usuario.builder().id(1L).username("vendedor").build();
        Producto producto = Producto.builder()
                .id(1L).nombre("Producto Test").precio(BigDecimal.TEN)
                .stock(2).activo(true).build();

        when(clienteRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(producto));

        VentaRequest request = VentaRequest.builder()
                .clienteId(1L)
                .usuarioId(1L)
                .items(List.of(
                        VentaRequest.DetalleRequest.builder().productoId(1L).cantidad(5).build()
                ))
                .build();

        assertThatThrownBy(() -> ventaService.crear(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stock insuficiente")
                .hasMessageContaining("Producto Test");

        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear lanza excepcion cuando el cliente no existe")
    void crear_clienteNoExiste() {
        when(clienteRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        VentaRequest request = VentaRequest.builder()
                .clienteId(99L).usuarioId(1L)
                .items(List.of(VentaRequest.DetalleRequest.builder().productoId(1L).cantidad(1).build()))
                .build();

        assertThatThrownBy(() -> ventaService.crear(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Cliente");
    }

    @Test
    @DisplayName("crear lanza excepcion cuando el producto no existe")
    void crear_productoNoExiste() {
        when(clienteRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(Cliente.builder().id(1L).build()));
        when(usuarioRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(Usuario.builder().id(1L).build()));
        when(productoRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        VentaRequest request = VentaRequest.builder()
                .clienteId(1L).usuarioId(1L)
                .items(List.of(VentaRequest.DetalleRequest.builder().productoId(99L).cantidad(1).build()))
                .build();

        assertThatThrownBy(() -> ventaService.crear(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Producto");
    }

    @Test
    @DisplayName("anular desactiva la venta y revierte el stock de cada producto")
    void anular_exitoso() {
        Producto producto = Producto.builder()
                .id(1L).nombre("Producto Test").precio(BigDecimal.TEN)
                .stock(7).activo(true).build();
        DetalleVenta detalle = DetalleVenta.builder()
                .id(10L).producto(producto).cantidad(3)
                .precioUnitario(BigDecimal.TEN).subtotal(new BigDecimal("30")).build();
        Venta venta = Venta.builder()
                .id(1L).fecha(LocalDateTime.now()).total(new BigDecimal("30"))
                .cliente(Cliente.builder().id(1L).build())
                .usuario(Usuario.builder().id(1L).build())
                .detalles(new java.util.ArrayList<>(List.of(detalle)))
                .activo(true).build();
        detalle.setVenta(venta);
        when(ventaRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(venta));

        ventaService.anular(1L);

        assertThat(venta.getActivo()).isFalse();
        assertThat(producto.getStock()).isEqualTo(10);
        verify(productoRepository).save(producto);
        verify(ventaRepository).save(venta);
    }

    @Test
    @DisplayName("anular lanza excepcion cuando la venta no existe o ya esta anulada")
    void anular_noExiste() {
        when(ventaRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.anular(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(productoRepository, never()).save(any());
        verify(ventaRepository, never()).save(any());
    }
}
