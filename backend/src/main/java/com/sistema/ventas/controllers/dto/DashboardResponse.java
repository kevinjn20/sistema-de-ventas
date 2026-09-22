package com.sistema.ventas.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private long totalVentas;
    private BigDecimal ingresosTotales;
    private long clientesActivos;
    private long productosActivos;
    private long usuariosActivos;
    private List<VentaDiaria> ventasPorDia;
    private List<TopProducto> topProductos;
    private List<ProductoInfo> productosBajoStock;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VentaDiaria {
        private LocalDate fecha;
        private BigDecimal total;
        private long cantidad;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopProducto {
        private Long productoId;
        private String nombre;
        private long cantidadVendida;
        private BigDecimal totalGenerado;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductoInfo {
        private Long id;
        private String nombre;
        private int stock;
    }
}
