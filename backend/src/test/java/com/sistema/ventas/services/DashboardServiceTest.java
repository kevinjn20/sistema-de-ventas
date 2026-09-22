package com.sistema.ventas.services;

import com.sistema.ventas.controllers.dto.DashboardResponse;
import com.sistema.ventas.controllers.dto.DashboardResponse.ProductoInfo;
import com.sistema.ventas.controllers.dto.DashboardResponse.TopProducto;
import com.sistema.ventas.controllers.dto.DashboardResponse.VentaDiaria;
import com.sistema.ventas.entities.Producto;
import com.sistema.ventas.repositories.ClienteRepository;
import com.sistema.ventas.repositories.ProductoRepository;
import com.sistema.ventas.repositories.UsuarioRepository;
import com.sistema.ventas.repositories.VentaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private VentaRepository ventaRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("obtenerDashboard devuelve todas las metricas correctamente")
    void obtenerDashboard_exitoso() {
        when(ventaRepository.countByActivoTrue()).thenReturn(10L);
        when(ventaRepository.sumTotalByActivoTrue()).thenReturn(new BigDecimal("5000"));
        when(clienteRepository.countByActivoTrue()).thenReturn(25L);
        when(productoRepository.countByActivoTrue()).thenReturn(50L);
        when(usuarioRepository.countByActivoTrue()).thenReturn(5L);

        when(ventaRepository.findVentasAgrupadasPorDia()).thenReturn(List.of(
                new Object[]{Date.valueOf("2026-07-01"), new BigDecimal("1500"), 3L},
                new Object[]{Date.valueOf("2026-07-02"), new BigDecimal("3500"), 7L}
        ));

        when(ventaRepository.findTopProductosVendidos()).thenReturn(List.of(
                new Object[]{1L, "Producto A", 20L, new BigDecimal("2000")},
                new Object[]{2L, "Producto B", 15L, new BigDecimal("1500")}
        ));

        when(productoRepository.findByActivoTrueAndStockLessThanEqual(5)).thenReturn(List.of(
                Producto.builder().id(3L).nombre("Producto C").stock(2).build()
        ));

        DashboardResponse result = dashboardService.obtenerDashboard();

        assertThat(result.getTotalVentas()).isEqualTo(10);
        assertThat(result.getIngresosTotales()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(result.getClientesActivos()).isEqualTo(25);
        assertThat(result.getProductosActivos()).isEqualTo(50);
        assertThat(result.getUsuariosActivos()).isEqualTo(5);

        assertThat(result.getVentasPorDia()).hasSize(2);
        VentaDiaria dia1 = result.getVentasPorDia().get(0);
        assertThat(dia1.getFecha()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(dia1.getTotal()).isEqualByComparingTo("1500");
        assertThat(dia1.getCantidad()).isEqualTo(3);

        assertThat(result.getTopProductos()).hasSize(2);
        TopProducto top1 = result.getTopProductos().get(0);
        assertThat(top1.getProductoId()).isEqualTo(1L);
        assertThat(top1.getNombre()).isEqualTo("Producto A");
        assertThat(top1.getCantidadVendida()).isEqualTo(20);

        assertThat(result.getProductosBajoStock()).hasSize(1);
        ProductoInfo bajoStock = result.getProductosBajoStock().get(0);
        assertThat(bajoStock.getNombre()).isEqualTo("Producto C");
        assertThat(bajoStock.getStock()).isEqualTo(2);
    }

    @Test
    @DisplayName("obtenerDashboard retorna ceros cuando no hay datos")
    void obtenerDashboard_vacio() {
        when(ventaRepository.countByActivoTrue()).thenReturn(0L);
        when(ventaRepository.sumTotalByActivoTrue()).thenReturn(BigDecimal.ZERO);
        when(clienteRepository.countByActivoTrue()).thenReturn(0L);
        when(productoRepository.countByActivoTrue()).thenReturn(0L);
        when(usuarioRepository.countByActivoTrue()).thenReturn(0L);
        when(ventaRepository.findVentasAgrupadasPorDia()).thenReturn(List.of());
        when(ventaRepository.findTopProductosVendidos()).thenReturn(List.of());
        when(productoRepository.findByActivoTrueAndStockLessThanEqual(5)).thenReturn(List.of());

        DashboardResponse result = dashboardService.obtenerDashboard();

        assertThat(result.getTotalVentas()).isZero();
        assertThat(result.getIngresosTotales()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getVentasPorDia()).isEmpty();
        assertThat(result.getTopProductos()).isEmpty();
        assertThat(result.getProductosBajoStock()).isEmpty();
    }

    @Test
    @DisplayName("obtenerDashboard con rango usa las consultas filtradas por fecha")
    void obtenerDashboard_conRango() {
        when(ventaRepository.countByActivoTrueAndFechaBetween(any(), any())).thenReturn(4L);
        when(ventaRepository.sumTotalByActivoTrueAndFechaBetween(any(), any()))
                .thenReturn(new BigDecimal("1200"));
        when(clienteRepository.countByActivoTrue()).thenReturn(10L);
        when(productoRepository.countByActivoTrue()).thenReturn(20L);
        when(usuarioRepository.countByActivoTrue()).thenReturn(3L);
        when(ventaRepository.findVentasAgrupadasPorDia(any(), any())).thenReturn(List.<Object[]>of(
                new Object[]{Date.valueOf("2026-08-01"), new BigDecimal("1200"), 4L}
        ));
        when(ventaRepository.findTopProductosVendidos(any(), any())).thenReturn(List.<Object[]>of(
                new Object[]{1L, "Producto A", 4L, new BigDecimal("1200")}
        ));
        when(productoRepository.findByActivoTrueAndStockLessThanEqual(5)).thenReturn(List.of());

        DashboardResponse result = dashboardService.obtenerDashboard(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertThat(result.getTotalVentas()).isEqualTo(4);
        assertThat(result.getIngresosTotales()).isEqualByComparingTo(new BigDecimal("1200"));
        assertThat(result.getVentasPorDia()).hasSize(1);
        assertThat(result.getVentasPorDia().get(0).getFecha()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(result.getTopProductos()).hasSize(1);
    }
}
