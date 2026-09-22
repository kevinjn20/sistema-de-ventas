package com.sistema.ventas.repositories;

import com.sistema.ventas.entities.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrue();

    Optional<Producto> findByIdAndActivoTrue(Long id);

    long countByActivoTrue();

    List<Producto> findByActivoTrueAndStockLessThanEqual(int stockMinimo);
}
