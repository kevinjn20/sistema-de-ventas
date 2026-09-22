package com.sistema.ventas.services;

import com.sistema.ventas.controllers.dto.ClienteRequest;
import com.sistema.ventas.controllers.dto.ClienteResponse;
import com.sistema.ventas.entities.Cliente;
import com.sistema.ventas.repositories.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public List<ClienteResponse> listarActivos() {
        return clienteRepository.findByActivoTrue()
                .stream()
                .map(ClienteResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(Long id) {
        Cliente cliente = clienteRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con id: " + id));
        return ClienteResponse.fromEntity(cliente);
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            clienteRepository.findByEmail(request.getEmail())
                    .ifPresent(c -> {
                        throw new DataIntegrityViolationException("El email ya está registrado: " + request.getEmail());
                    });
        }
        Cliente cliente = Cliente.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
                .build();
        return ClienteResponse.fromEntity(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con id: " + id));
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(cliente.getEmail())) {
            clienteRepository.findByEmail(request.getEmail())
                    .ifPresent(c -> {
                        throw new DataIntegrityViolationException("El email ya está registrado: " + request.getEmail());
                    });
        }
        cliente.setNombre(request.getNombre());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        return ClienteResponse.fromEntity(clienteRepository.save(cliente));
    }

    @Transactional
    public void eliminar(Long id) {
        Cliente cliente = clienteRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con id: " + id));
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }
}
