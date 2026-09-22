package com.sistema.ventas.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.ventas.controllers.dto.ProductoResponse;
import com.sistema.ventas.security.JwtAuthFilter;
import com.sistema.ventas.services.ProductoService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ProductoService productoService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private ProductoResponse respuesta() {
        return ProductoResponse.builder()
                .id(1L).nombre("Laptop").descripcion("Gama alta")
                .precio(new BigDecimal("1500.00")).stock(10).activo(true)
                .build();
    }

    @Test
    @DisplayName("GET /api/productos devuelve la lista con 200")
    void listar_200() throws Exception {
        when(productoService.listarActivos()).thenReturn(List.of(respuesta()));

        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Laptop"))
                .andExpect(jsonPath("$[0].precio").value(1500.00));
    }

    @Test
    @DisplayName("GET /api/productos/99 inexistente devuelve 404 estandarizado")
    void obtener_noExiste_404() throws Exception {
        when(productoService.obtenerPorId(99L))
                .thenThrow(new EntityNotFoundException("Producto no encontrado con id: 99"));

        mvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("POST /api/productos valido devuelve 201")
    void crear_valido_201() throws Exception {
        when(productoService.crear(any())).thenReturn(respuesta());

        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Laptop","descripcion":"Gama alta","precio":1500.00,"stock":10}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/productos sin nombre ni precio devuelve 400 con detalle de campos")
    void crear_invalido_400() throws Exception {
        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"descripcion":"sin nombre ni precio"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.errors.nombre").exists())
                .andExpect(jsonPath("$.errors.precio").exists());
    }

    @Test
    @DisplayName("POST /api/productos con cantidad negativa en stock devuelve 400")
    void crear_stockNegativo_400() throws Exception {
        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Laptop","precio":1500.00,"stock":-5}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.stock").exists());
    }

    @Test
    @DisplayName("DELETE /api/productos/1 devuelve 204")
    void eliminar_204() throws Exception {
        mvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /api/productos/1 valido devuelve 200")
    void actualizar_200() throws Exception {
        when(productoService.actualizar(eq(1L), any())).thenReturn(respuesta());

        mvc.perform(put("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Laptop Pro","precio":1700.00,"stock":8}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laptop"));
    }
}
