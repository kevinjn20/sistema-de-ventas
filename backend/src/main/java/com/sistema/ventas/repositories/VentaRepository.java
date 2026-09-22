package com.sistema.ventas.repositories;

import com.sistema.ventas.entities.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findByActivoTrueOrderByCreatedAtDesc();

    Page<Venta> findByActivoTrueOrderByCreatedAtDesc(Pageable pageable);

    List<Venta> findByActivoTrueAndFechaBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);

    Optional<Venta> findByIdAndActivoTrue(Long id);

    long countByActivoTrue();

    long countByActivoTrueAndFechaBetween(LocalDateTime from, LocalDateTime to);

    @Query(value = """
            SELECT CAST(v.fecha AS DATE) AS fecha,
                   SUM(v.total) AS total,
                   COUNT(v.id) AS cantidad
            FROM ventas v
            WHERE v.activo = true
            GROUP BY CAST(v.fecha AS DATE)
            ORDER BY CAST(v.fecha AS DATE) ASC
            """, nativeQuery = true)
    List<Object[]> findVentasAgrupadasPorDia();

    @Query(value = """
            SELECT CAST(v.fecha AS DATE) AS fecha,
                   SUM(v.total) AS total,
                   COUNT(v.id) AS cantidad
            FROM ventas v
            WHERE v.activo = true
              AND v.fecha BETWEEN :from AND :to
            GROUP BY CAST(v.fecha AS DATE)
            ORDER BY CAST(v.fecha AS DATE) ASC
            """, nativeQuery = true)
    List<Object[]> findVentasAgrupadasPorDia(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT p.id AS producto_id,
                   p.nombre AS nombre,
                   SUM(dv.cantidad) AS cantidad_vendida,
                   SUM(dv.subtotal) AS total_generado
            FROM detalles_venta dv
            JOIN productos p ON p.id = dv.producto_id
            JOIN ventas v ON v.id = dv.venta_id
            WHERE v.activo = true
            GROUP BY p.id, p.nombre
            ORDER BY SUM(dv.cantidad) DESC
            LIMIT 5
            """, nativeQuery = true)
    List<Object[]> findTopProductosVendidos();

    @Query(value = """
            SELECT p.id AS producto_id,
                   p.nombre AS nombre,
                   SUM(dv.cantidad) AS cantidad_vendida,
                   SUM(dv.subtotal) AS total_generado
            FROM detalles_venta dv
            JOIN productos p ON p.id = dv.producto_id
            JOIN ventas v ON v.id = dv.venta_id
            WHERE v.activo = true
              AND v.fecha BETWEEN :from AND :to
            GROUP BY p.id, p.nombre
            ORDER BY SUM(dv.cantidad) DESC
            LIMIT 5
            """, nativeQuery = true)
    List<Object[]> findTopProductosVendidos(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.activo = true")
    BigDecimal sumTotalByActivoTrue();

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.activo = true AND v.fecha BETWEEN :from AND :to")
    BigDecimal sumTotalByActivoTrueAndFechaBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
