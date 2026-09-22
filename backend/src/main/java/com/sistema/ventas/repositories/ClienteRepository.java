package com.sistema.ventas.repositories;

import com.sistema.ventas.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByActivoTrue();

    Optional<Cliente> findByIdAndActivoTrue(Long id);

    Optional<Cliente> findByEmail(String email);

    long countByActivoTrue();
}
