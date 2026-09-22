package com.sistema.ventas.config;

import com.sistema.ventas.entities.Usuario;
import com.sistema.ventas.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByUsername(adminUsername).isEmpty()) {
            Usuario admin = Usuario.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .nombre("Administrador")
                    .rol("ADMIN")
                    .build();
            usuarioRepository.save(admin);
            log.info("Usuario admin creado por defecto (username={})", adminUsername);
        }
    }
}