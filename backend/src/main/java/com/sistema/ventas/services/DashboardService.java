package com.sistema.ventas.services;

import com.sistema.ventas.controllers.dto.DashboardResponse;
import com.sistema.ventas.controllers.dto.DashboardResponse.ProductoInfo;
import com.sistema.ventas.controllers.dto.DashboardResponse.TopProducto;
import com.sistema.ventas.controllers.dto.DashboardResponse.VentaDiaria;
import com.sistema.ventas.repositories.ClienteRepository;
import com.sistema.ventas.repositories.ProductoRepository;
import com.sistema.ventas.repositories.UsuarioRepository;
import com.sistema.ventas.repositories.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    @Value("${app.dashboard.stock-minimo:5}")
    private int stockMinimo = 5;

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public DashboardResponse obtenerDashboard() {
        long totalVentas = ventaRepository.countByActivoTrue();
        BigDecimal ingresosTotales = ventaRepository.sumTotalByActivoTrue();
        long clientesActivos = clienteRepository.countByActivoTrue();
        long productosActivos = productoRepository.countByActivoTrue();
        long usuariosActivos = usuarioRepository.countByActivoTrue();

        List<VentaDiaria> ventasPorDia = ventaRepository.findVentasAgrupadasPorDia()
                .stream()
                .map(row -> VentaDiaria.builder()
                        .fecha(((Date) row[0]).toLocalDate())
                        .total((BigDecimal) row[1])
                        .cantidad(((Number) row[2]).longValue())
                        .build())
                .toList();

        List<TopProducto> topProductos = ventaRepository.findTopProductosVendidos()
                .stream()
                .map(row -> TopProducto.builder()
                        .productoId(((Number) row[0]).longValue())
                        .nombre((String) row[1])
                        .cantidadVendida(((Number) row[2]).longValue())
                        .totalGenerado((BigDecimal) row[3])
                        .build())
                .toList();

        return DashboardResponse.builder()
                .totalVentas(totalVentas)
                .ingresosTotales(ingresosTotales)
                .clientesActivos(clientesActivos)
                .productosActivos(productosActivos)
                .usuariosActivos(usuariosActivos)
                .ventasPorDia(ventasPorDia)
                .topProductos(topProductos)
                .productosBajoStock(productosBajoStock())
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardResponse obtenerDashboard(LocalDate from, LocalDate to) {
        LocalDate resolvedTo = to != null ? to : LocalDate.now();
        LocalDate resolvedFrom = from != null ? from : resolvedTo.minusDays(30);
        LocalDateTime fromDateTime = resolvedFrom.atStartOfDay();
        LocalDateTime toDateTime = resolvedTo.plusDays(1).atStartOfDay().minusNanos(1);

        long totalVentas = ventaRepository.countByActivoTrueAndFechaBetween(fromDateTime, toDateTime);
        BigDecimal ingresosTotales = ventaRepository.sumTotalByActivoTrueAndFechaBetween(fromDateTime, toDateTime);
        long clientesActivos = clienteRepository.countByActivoTrue();
        long productosActivos = productoRepository.countByActivoTrue();
        long usuariosActivos = usuarioRepository.countByActivoTrue();

        List<VentaDiaria> ventasPorDia = ventaRepository.findVentasAgrupadasPorDia(fromDateTime, toDateTime)
                .stream()
                .map(row -> VentaDiaria.builder()
                        .fecha(((Date) row[0]).toLocalDate())
                        .total((BigDecimal) row[1])
                        .cantidad(((Number) row[2]).longValue())
                        .build())
                .toList();

        List<TopProducto> topProductos = ventaRepository.findTopProductosVendidos(fromDateTime, toDateTime)
                .stream()
                .map(row -> TopProducto.builder()
                        .productoId(((Number) row[0]).longValue())
                        .nombre((String) row[1])
                        .cantidadVendida(((Number) row[2]).longValue())
                        .totalGenerado((BigDecimal) row[3])
                        .build())
                .toList();

        return DashboardResponse.builder()
                .totalVentas(totalVentas)
                .ingresosTotales(ingresosTotales)
                .clientesActivos(clientesActivos)
                .productosActivos(productosActivos)
                .usuariosActivos(usuariosActivos)
                .ventasPorDia(ventasPorDia)
                .topProductos(topProductos)
                .productosBajoStock(productosBajoStock())
                .build();
    }

    private List<ProductoInfo> productosBajoStock() {
        return productoRepository
                .findByActivoTrueAndStockLessThanEqual(stockMinimo)
                .stream()
                .map(p -> ProductoInfo.builder()
                        .id(p.getId())
                        .nombre(p.getNombre())
                        .stock(p.getStock())
                        .build())
                .toList();
    }
}
