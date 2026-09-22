package com.sistema.ventas.repositories;

import com.sistema.ventas.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findByActivoTrue();

    Optional<Usuario> findByIdAndActivoTrue(Long id);

    Optional<Usuario> findByUsername(String username);

    long countByActivoTrue();
}
