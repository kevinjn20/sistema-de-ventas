package com.sistema.ventas.services;

import com.sistema.ventas.controllers.dto.ProductoRequest;
import com.sistema.ventas.controllers.dto.ProductoResponse;
import com.sistema.ventas.entities.Producto;
import com.sistema.ventas.repositories.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    @DisplayName("listarActivos devuelve solo productos activos")
    void listarActivos() {
        Producto p1 = Producto.builder().id(1L).nombre("Producto 1").precio(BigDecimal.TEN).stock(5).activo(true).build();
        Producto p2 = Producto.builder().id(2L).nombre("Producto 2").precio(BigDecimal.ONE).stock(10).activo(true).build();
        when(productoRepository.findByActivoTrue()).thenReturn(List.of(p1, p2));

        List<ProductoResponse> result = productoService.listarActivos();

        assertThat(result).hasSize(2);
        verify(productoRepository).findByActivoTrue();
    }

    @Test
    @DisplayName("obtenerPorId lanza excepcion cuando el producto no existe")
    void obtenerPorId_noExiste() {
        when(productoRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtenerPorId(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("crear persiste y devuelve el producto")
    void crear() {
        ProductoRequest request = ProductoRequest.builder()
                .nombre("Nuevo Producto")
                .precio(new BigDecimal("25.50"))
                .stock(100)
                .build();

        Producto entity = Producto.builder()
                .id(1L)
                .nombre("Nuevo Producto")
                .precio(new BigDecimal("25.50"))
                .stock(100)
                .activo(true)
                .build();

        when(productoRepository.save(any(Producto.class))).thenReturn(entity);

        ProductoResponse result = productoService.crear(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Nuevo Producto");
        assertThat(result.getPrecio()).isEqualByComparingTo(new BigDecimal("25.50"));
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    @DisplayName("actualizar modifica y devuelve el producto actualizado")
    void actualizar() {
        Long id = 1L;
        ProductoRequest request = ProductoRequest.builder()
                .nombre("Actualizado")
                .precio(new BigDecimal("99.99"))
                .stock(1)
                .build();

        Producto existing = Producto.builder()
                .id(id)
                .nombre("Original")
                .precio(BigDecimal.ONE)
                .stock(10)
                .activo(true)
                .build();

        when(productoRepository.findByIdAndActivoTrue(id)).thenReturn(Optional.of(existing));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductoResponse result = productoService.actualizar(id, request);

        assertThat(result.getNombre()).isEqualTo("Actualizado");
        assertThat(result.getPrecio()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(result.getStock()).isEqualTo(1);
    }

    @Test
    @DisplayName("eliminar hace borrado logico (activo = false)")
    void eliminar() {
        Long id = 1L;
        Producto producto = Producto.builder().id(id).activo(true).build();
        when(productoRepository.findByIdAndActivoTrue(id)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        productoService.eliminar(id);

        assertThat(producto.getActivo()).isFalse();
        verify(productoRepository).save(producto);
    }
}
