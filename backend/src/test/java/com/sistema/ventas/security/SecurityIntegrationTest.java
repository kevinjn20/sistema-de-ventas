package com.sistema.ventas.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.ventas.controllers.dto.LoginRequest;
import com.sistema.ventas.entities.Usuario;
import com.sistema.ventas.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        if (usuarioRepository.findByUsername("vendedor_it").isEmpty()) {
            usuarioRepository.save(Usuario.builder()
                    .username("vendedor_it")
                    .password(passwordEncoder.encode("vendedor123"))
                    .nombre("Vendedor IT")
                    .rol("VENDEDOR")
                    .build());
        }
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    @DisplayName("login con credenciales incorrectas devuelve 401")
    void loginBadCredentials_401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new LoginRequest("admin", "clave-mal"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("GET /api/productos sin token devuelve 401")
    void sinToken_401() throws Exception {
        mvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/productos con token manipulado devuelve 401")
    void tokenManipulado_401() throws Exception {
        mvc.perform(get("/api/productos")
                        .header("Authorization", "Bearer token-manipulado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("login admin permite acceder a /api/productos con Bearer")
    void loginAdmin_accede_200() throws Exception {
        String token = login("admin", "admin123");
        assertThat(token).isNotBlank();

        mvc.perform(get("/api/productos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("VENDEDOR no puede crear usuarios (solo ADMIN) -> 403")
    void vendedor_noPuedeCrearUsuarios_403() throws Exception {
        String token = login("vendedor_it", "vendedor123");

        mvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"nuevo_it","password":"secreto123","nombre":"Nuevo","rol":"VENDEDOR"}"""))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("VENDEDOR supera seguridad y validacion en ventas (404 por cliente inexistente, no 401/403)")
    void vendedor_pasaSeguridadEnVentas() throws Exception {
        String token = login("vendedor_it", "vendedor123");

        mvc.perform(post("/api/ventas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":999,"usuarioId":999,"items":[{"productoId":999,"cantidad":1}]}"""))
                .andExpect(status().isNotFound());
    }
}
