package com.sistema.ventas.services;

import com.sistema.ventas.controllers.dto.VentaRequest;
import com.sistema.ventas.controllers.dto.VentaResponse;
import com.sistema.ventas.entities.*;
import com.sistema.ventas.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<VentaResponse> listarActivos() {
        return ventaRepository.findByActivoTrueOrderByCreatedAtDesc()
                .stream()
                .map(VentaResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<VentaResponse> listarActivos(Pageable pageable) {
        return ventaRepository.findByActivoTrueOrderByCreatedAtDesc(pageable)
                .map(VentaResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<VentaResponse> listarActivosEntre(LocalDateTime from, LocalDateTime to) {
        return ventaRepository.findByActivoTrueAndFechaBetweenOrderByCreatedAtDesc(from, to)
                .stream()
                .map(VentaResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public VentaResponse obtenerPorId(Long id) {
        Venta venta = ventaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Venta no encontrada con id: " + id));
        return VentaResponse.fromEntity(venta);
    }

    @Transactional
    public VentaResponse crear(VentaRequest request) {
        Cliente cliente = clienteRepository.findByIdAndActivoTrue(request.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con id: " + request.getClienteId()));

        Usuario usuario = usuarioRepository.findByIdAndActivoTrue(request.getUsuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + request.getUsuarioId()));

        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (VentaRequest.DetalleRequest item : request.getItems()) {
            Producto producto = productoRepository.findByIdAndActivoTrue(item.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + item.getProductoId()));

            if (producto.getStock() < item.getCantidad()) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para el producto '" + producto.getNombre()
                                + "': disponible " + producto.getStock()
                                + ", solicitado " + item.getCantidad());
            }

            BigDecimal precioUnitario = producto.getPrecio();
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(subtotal);

            DetalleVenta detalle = DetalleVenta.builder()
                    .producto(producto)
                    .cantidad(item.getCantidad())
                    .precioUnitario(precioUnitario)
                    .subtotal(subtotal)
                    .build();
            detalles.add(detalle);
        }

        Venta venta = Venta.builder()
                .fecha(LocalDateTime.now())
                .total(total)
                .cliente(cliente)
                .usuario(usuario)
                .detalles(detalles)
                .build();

        detalles.forEach(d -> d.setVenta(venta));

        Venta saved = ventaRepository.save(venta);

        for (DetalleVenta detalle : detalles) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() - detalle.getCantidad());
            productoRepository.save(producto);
        }

        return VentaResponse.fromEntity(saved);
    }

    @Transactional
    public void anular(Long id) {
        Venta venta = ventaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Venta no encontrada con id: " + id));
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);
        }
        venta.setActivo(false);
        ventaRepository.save(venta);
    }
}
