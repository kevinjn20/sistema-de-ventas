package com.sistema.ventas.controllers;

import com.sistema.ventas.controllers.dto.VentaRequest;
import com.sistema.ventas.controllers.dto.VentaResponse;
import com.sistema.ventas.services.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @GetMapping
    public ResponseEntity<List<VentaResponse>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from != null && to != null) {
            return ResponseEntity.ok(ventaService.listarActivosEntre(from.atStartOfDay(), to.plusDays(1).atStartOfDay().minusNanos(1)));
        }
        return ResponseEntity.ok(ventaService.listarActivos());
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<VentaResponse>> listarPaginado(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ventaService.listarActivos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<VentaResponse> crear(@Valid @RequestBody VentaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.crear(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> anular(@PathVariable Long id) {
        ventaService.anular(id);
        return ResponseEntity.noContent().build();
    }
}
