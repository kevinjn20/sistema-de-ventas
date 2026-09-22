package com.sistema.ventas.services;

import com.sistema.ventas.controllers.dto.VentaRequest;
import com.sistema.ventas.controllers.dto.VentaResponse;
import com.sistema.ventas.entities.Cliente;
import com.sistema.ventas.entities.Producto;
import com.sistema.ventas.entities.Usuario;
import com.sistema.ventas.repositories.ClienteRepository;
import com.sistema.ventas.repositories.ProductoRepository;
import com.sistema.ventas.repositories.UsuarioRepository;
import com.sistema.ventas.repositories.VentaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VentaServiceIT {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Cliente cliente;
    private Usuario usuario;
    private Producto producto;

    @BeforeEach
    void setUp() {
        cliente = clienteRepository.save(Cliente.builder().nombre("Cliente IT").build());
        usuario = usuarioRepository.save(Usuario.builder()
                .username("vendedor_it_ventas")
                .password(passwordEncoder.encode("clave123"))
                .nombre("Vendedor IT")
                .rol("VENDEDOR")
                .build());
        producto = productoRepository.save(Producto.builder()
                .nombre("Producto IT")
                .precio(new BigDecimal("100.00"))
                .stock(10)
                .build());
    }

    private VentaRequest ventaDe(int cantidad) {
        return VentaRequest.builder()
                .clienteId(cliente.getId())
                .usuarioId(usuario.getId())
                .items(List.of(VentaRequest.DetalleRequest.builder()
                        .productoId(producto.getId())
                        .cantidad(cantidad)
                        .build()))
                .build();
    }

    @Test
    @DisplayName("crear persiste cabecera+detalle y descuenta stock en la misma transaccion")
    void crear_descuentaStock() {
        VentaResponse response = ventaService.crear(ventaDe(3));

        assertThat(response.getId()).isNotNull();
        assertThat(response.getTotal()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(productoRepository.findById(producto.getId()).orElseThrow().getStock()).isEqualTo(7);
        assertThat(ventaRepository.findById(response.getId())).isPresent();
    }

    @Test
    @DisplayName("crear con stock insuficiente no persiste nada")
    void crear_stockInsuficiente_rollback() {
        long ventasAntes = ventaRepository.count();

        assertThatThrownBy(() -> ventaService.crear(ventaDe(99)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stock insuficiente");

        assertThat(ventaRepository.count()).isEqualTo(ventasAntes);
        assertThat(productoRepository.findById(producto.getId()).orElseThrow().getStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("anular desactiva la venta y revierte el stock; segunda anulacion da 404")
    void anular_revierteStock() {
        VentaResponse response = ventaService.crear(ventaDe(4));
        assertThat(productoRepository.findById(producto.getId()).orElseThrow().getStock()).isEqualTo(6);

        ventaService.anular(response.getId());

        assertThat(ventaRepository.findById(response.getId()).orElseThrow().getActivo()).isFalse();
        assertThat(productoRepository.findById(producto.getId()).orElseThrow().getStock()).isEqualTo(10);
        assertThat(ventaRepository.findByIdAndActivoTrue(response.getId())).isEmpty();

        assertThatThrownBy(() -> ventaService.anular(response.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
