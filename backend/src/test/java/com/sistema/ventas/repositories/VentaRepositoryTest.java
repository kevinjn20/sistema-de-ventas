package com.sistema.ventas.repositories;

import com.sistema.ventas.entities.Cliente;
import com.sistema.ventas.entities.DetalleVenta;
import com.sistema.ventas.entities.Producto;
import com.sistema.ventas.entities.Usuario;
import com.sistema.ventas.entities.Venta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class VentaRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private VentaRepository ventaRepository;

    private Producto producto;
    private Cliente cliente;
    private Usuario usuario;

    private Venta guardarVenta(LocalDateTime fecha, int cantidad, String precio) {
        Venta venta = Venta.builder()
                .fecha(fecha)
                .total(new BigDecimal(precio).multiply(BigDecimal.valueOf(cantidad)))
                .cliente(cliente)
                .usuario(usuario)
                .build();
        DetalleVenta detalle = DetalleVenta.builder()
                .venta(venta)
                .producto(producto)
                .cantidad(cantidad)
                .precioUnitario(new BigDecimal(precio))
                .subtotal(new BigDecimal(precio).multiply(BigDecimal.valueOf(cantidad)))
                .build();
        venta.setDetalles(new java.util.ArrayList<>(List.of(detalle)));
        return em.persistAndFlush(venta);
    }

    @BeforeEach
    void setUp() {
        cliente = em.persistAndFlush(Cliente.builder().nombre("Cliente Repo").build());
        usuario = em.persistAndFlush(Usuario.builder()
                .username("repo_user").password("hash").rol("VENDEDOR").build());
        producto = em.persistAndFlush(Producto.builder()
                .nombre("Producto Repo").precio(new BigDecimal("50.00")).stock(100).build());
        em.clear();
    }

    @Test
    @DisplayName("consultas por rango filtran por fecha y agregan totales")
    void consultasPorRango() {
        guardarVenta(LocalDateTime.of(2026, 8, 5, 10, 0), 2, "50.00");
        guardarVenta(LocalDateTime.of(2026, 8, 20, 10, 0), 1, "50.00");
        guardarVenta(LocalDateTime.of(2026, 9, 2, 10, 0), 4, "50.00");
        em.clear();

        LocalDateTime from = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 8, 31, 23, 59, 59);

        assertThat(ventaRepository.countByActivoTrue()).isEqualTo(3);
        assertThat(ventaRepository.countByActivoTrueAndFechaBetween(from, to)).isEqualTo(2);
        assertThat(ventaRepository.sumTotalByActivoTrueAndFechaBetween(from, to))
                .isEqualByComparingTo(new BigDecimal("150.00"));

        List<Venta> agosto = ventaRepository.findByActivoTrueAndFechaBetweenOrderByCreatedAtDesc(from, to);
        assertThat(agosto).hasSize(2);

        assertThat(ventaRepository.findVentasAgrupadasPorDia(from, to)).hasSize(2);
        assertThat(ventaRepository.findVentasAgrupadasPorDia()).hasSize(3);

        List<Object[]> top = ventaRepository.findTopProductosVendidos(from, to);
        assertThat(top).hasSize(1);
        assertThat(((Number) top.get(0)[2]).longValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("venta anulada (activo=false) queda excluida de conteos y agregaciones")
    void anuladaExcluida() {
        Venta venta = guardarVenta(LocalDateTime.of(2026, 8, 10, 10, 0), 2, "50.00");
        em.clear();

        LocalDateTime from = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 8, 31, 23, 59, 59);
        assertThat(ventaRepository.countByActivoTrueAndFechaBetween(from, to)).isEqualTo(1);

        venta.setActivo(false);
        ventaRepository.saveAndFlush(venta);
        em.clear();

        assertThat(ventaRepository.countByActivoTrueAndFechaBetween(from, to)).isZero();
        assertThat(ventaRepository.sumTotalByActivoTrueAndFechaBetween(from, to))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(ventaRepository.findByIdAndActivoTrue(venta.getId())).isEmpty();
    }
}
