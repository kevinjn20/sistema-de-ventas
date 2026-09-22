package com.sistema.ventas.services;

import com.sistema.ventas.controllers.dto.UsuarioRequest;
import com.sistema.ventas.controllers.dto.UsuarioResponse;
import com.sistema.ventas.entities.Usuario;
import com.sistema.ventas.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarActivos() {
        return usuarioRepository.findByActivoTrue()
                .stream()
                .map(UsuarioResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
        return UsuarioResponse.fromEntity(usuario);
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (request.getUsername() != null) {
            usuarioRepository.findByUsername(request.getUsername())
                    .ifPresent(u -> {
                        throw new DataIntegrityViolationException("El username ya está registrado: " + request.getUsername());
                    });
        }
        String rol = (request.getRol() != null && !request.getRol().isBlank())
                ? request.getRol() : "VENDEDOR";
        Usuario usuario = Usuario.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nombre(request.getNombre())
                .rol(rol)
                .build();
        return UsuarioResponse.fromEntity(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
        if (request.getUsername() != null && !request.getUsername().equals(usuario.getUsername())) {
            usuarioRepository.findByUsername(request.getUsername())
                    .ifPresent(u -> {
                        throw new DataIntegrityViolationException("El username ya está registrado: " + request.getUsername());
                    });
        }
        if (request.getUsername() != null) {
            usuario.setUsername(request.getUsername());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getNombre() != null) {
            usuario.setNombre(request.getNombre());
        }
        if (request.getRol() != null && !request.getRol().isBlank()) {
            usuario.setRol(request.getRol());
        }
        return UsuarioResponse.fromEntity(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = usuarioRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }
}
