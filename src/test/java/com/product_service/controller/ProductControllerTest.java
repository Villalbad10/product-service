package com.product_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.product_service.dto.CreateProductRequest;
import com.product_service.model.Product;
import com.product_service.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@TestPropertySource(properties = {
        "server.port=0",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "api.key=test-key"
})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product buildProduct(Long id) {
        return Product.builder()
                .idProducto(id)
                .nombre("Teclado")
                .precio(new BigDecimal("99.99"))
                .descripcion("Mecánico")
                .eliminado(false)
                .build();
    }

    @Test
    @DisplayName("POST /save crea producto y retorna 201")
    void crearProducto_returnsCreated() throws Exception {
        CreateProductRequest toCreate = CreateProductRequest.builder()
                .nombre("Mouse")
                .precio(new BigDecimal("49.90"))
                .descripcion("Gamer")
                .build();
        Product created = Product.builder()
                .idProducto(1L)
                .nombre("Mouse")
                .precio(new BigDecimal("49.90"))
                .descripcion("Gamer")
                .eliminado(false)
                .build();

        Mockito.when(productService.crearProducto(any(CreateProductRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/products/save")
                        .header("X-API-KEY", "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProducto", is(1)))
                .andExpect(jsonPath("$.nombre", is("Mouse")));
    }

    @Test
    @DisplayName("GET /{id} retorna producto si existe")
    void obtenerProductoPorId_found() throws Exception {
        Product product = buildProduct(2L);
        Mockito.when(productService.buscarProductoPorId(2L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/v1/products/2")
                        .header("X-API-KEY", "test-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProducto", is(2)))
                .andExpect(jsonPath("$.nombre", is("Teclado")));
    }

    @Test
    @DisplayName("GET /{id} retorna 404 si no existe")
    void obtenerProductoPorId_notFound() throws Exception {
        Mockito.when(productService.buscarProductoPorId(3L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/products/3")
                        .header("X-API-KEY", "test-key"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /list retorna página de productos")
    void obtenerTodosLosProductos_page() throws Exception {
        Page<Product> page = new PageImpl<>(List.of(buildProduct(5L)), PageRequest.of(0, 10), 1);
        Mockito.when(productService.obtenerTodosLosProductos(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/products/list").header("X-API-KEY", "test-key")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].idProducto", is(5)));
    }

    @Test
    @DisplayName("PUT /update/{id} actualiza y retorna 200")
    void actualizarProducto_ok() throws Exception {
        Product update = Product.builder().nombre("Nuevo").precio(new BigDecimal("10.00")).build();
        Product updated = buildProduct(9L);
        updated.setNombre("Nuevo");
        updated.setPrecio(new BigDecimal("10.00"));

        Mockito.when(productService.actualizarProducto(eq(9L), any(Product.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/products/update/9")
                        .header("X-API-KEY", "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProducto", is(9)))
                .andExpect(jsonPath("$.nombre", is("Nuevo")));
    }

    @Test
    @DisplayName("DELETE /delete/{id} retorna 204")
    void eliminarProducto_noContent() throws Exception {
        mockMvc.perform(delete("/api/v1/products/delete/7")
                        .header("X-API-KEY", "test-key"))
                .andExpect(status().isNoContent());
        Mockito.verify(productService).eliminarProducto(7L);
    }
}


