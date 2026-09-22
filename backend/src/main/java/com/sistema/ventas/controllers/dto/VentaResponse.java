package com.sistema.ventas.controllers.dto;

import com.sistema.ventas.entities.DetalleVenta;
import com.sistema.ventas.entities.Venta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaResponse {

    private Long id;
    private LocalDateTime fecha;
    private BigDecimal total;
    private Boolean activo;
    private ClienteInfo cliente;
    private UsuarioInfo usuario;
    private List<DetalleInfo> detalles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClienteInfo {
        private Long id;
        private String nombre;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UsuarioInfo {
        private Long id;
        private String username;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetalleInfo {
        private Long id;
        private Long productoId;
        private String productoNombre;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }

    public static VentaResponse fromEntity(Venta venta) {
        ClienteInfo clienteInfo = ClienteInfo.builder()
                .id(venta.getCliente().getId())
                .nombre(venta.getCliente().getNombre())
                .build();

        UsuarioInfo usuarioInfo = UsuarioInfo.builder()
                .id(venta.getUsuario().getId())
                .username(venta.getUsuario().getUsername())
                .build();

        List<DetalleInfo> detalleInfos = venta.getDetalles().stream()
                .map(d -> DetalleInfo.builder()
                        .id(d.getId())
                        .productoId(d.getProducto().getId())
                        .productoNombre(d.getProducto().getNombre())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getSubtotal())
                        .build())
                .toList();

        return VentaResponse.builder()
                .id(venta.getId())
                .fecha(venta.getFecha())
                .total(venta.getTotal())
                .activo(venta.getActivo())
                .cliente(clienteInfo)
                .usuario(usuarioInfo)
                .detalles(detalleInfos)
                .createdAt(venta.getCreatedAt())
                .updatedAt(venta.getUpdatedAt())
                .build();
    }
}
