package com.sistema.ventas.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.ventas.controllers.dto.VentaResponse;
import com.sistema.ventas.security.JwtAuthFilter;
import com.sistema.ventas.services.VentaService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VentaController.class)
@AutoConfigureMockMvc(addFilters = false)
class VentaControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private VentaService ventaService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private VentaResponse respuesta() {
        return VentaResponse.builder()
                .id(1L).total(new BigDecimal("300.00")).activo(true)
                .cliente(VentaResponse.ClienteInfo.builder().id(1L).nombre("Cliente").build())
                .usuario(VentaResponse.UsuarioInfo.builder().id(1L).username("vendedor").build())
                .detalles(List.of(VentaResponse.DetalleInfo.builder()
                        .id(10L).productoId(1L).productoNombre("Producto")
                        .cantidad(3).precioUnitario(new BigDecimal("100.00"))
                        .subtotal(new BigDecimal("300.00")).build()))
                .build();
    }

    @Test
    @DisplayName("POST /api/ventas valido devuelve 201 con total calculado")
    void crear_valido_201() throws Exception {
        when(ventaService.crear(any())).thenReturn(respuesta());

        mvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1,"usuarioId":1,"items":[{"productoId":1,"cantidad":3}]}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(300.00))
                .andExpect(jsonPath("$.detalles[0].cantidad").value(3));
    }

    @Test
    @DisplayName("POST /api/ventas con cantidad 0 devuelve 400 (validacion @Min)")
    void crear_cantidadCero_400() throws Exception {
        mvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1,"usuarioId":1,"items":[{"productoId":1,"cantidad":0}]}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    @DisplayName("POST /api/ventas sin items devuelve 400")
    void crear_sinItems_400() throws Exception {
        mvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1,"usuarioId":1,"items":[]}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/ventas/99 inexistente devuelve 404")
    void obtener_noExiste_404() throws Exception {
        when(ventaService.obtenerPorId(99L))
                .thenThrow(new EntityNotFoundException("Venta no encontrada con id: 99"));

        mvc.perform(get("/api/ventas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /api/ventas/1 devuelve 204")
    void anular_204() throws Exception {
        mvc.perform(delete("/api/ventas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("serializacion de VentaResponse no expone password ni entidades internas")
    void respuesta_sinDatosSensibles() throws Exception {
        when(ventaService.obtenerPorId(1L)).thenReturn(respuesta());

        String body = mvc.perform(get("/api/ventas/1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assert !body.toLowerCase().contains("password");
    }
}
