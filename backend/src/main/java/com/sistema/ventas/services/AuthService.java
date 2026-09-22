package com.sistema.ventas.services;

import com.sistema.ventas.controllers.dto.LoginRequest;
import com.sistema.ventas.controllers.dto.LoginResponse;
import com.sistema.ventas.entities.Usuario;
import com.sistema.ventas.repositories.UsuarioRepository;
import com.sistema.ventas.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        String rol = usuario.getRol() != null ? usuario.getRol() : "VENDEDOR";
        String token = jwtService.generateToken(usuario.getUsername(), rol, usuario.getId());
        return LoginResponse.bearer(token, usuario.getUsername(), rol, usuario.getId());
    }
}
