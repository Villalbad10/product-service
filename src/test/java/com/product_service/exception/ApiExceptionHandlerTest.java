package com.product_service.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.product_service.controller.ProductController;
import com.product_service.model.Product;
import com.product_service.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@TestPropertySource(properties = {
        "server.port=0",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "api.key=test-key"
})
class ApiExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Validación @Valid produce 400 y lista de errores")
    void validationErrors_returnBadRequest() throws Exception {
        Product invalid = Product.builder()
                .nombre("") // NotBlank
                .precio(new BigDecimal("0")) // DecimalMin
                .build();

        mockMvc.perform(post("/api/v1/products/save")
                        .header("X-API-KEY", "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/products/save"));
        Mockito.verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("NotFoundException se mapea a 404 con mensaje")
    void notFoundException_mappedTo404() throws Exception {
        Mockito.when(productService.buscarProductoPorId(100L)).thenThrow(new NotFoundException("No encontrado"));

        mockMvc.perform(get("/api/v1/products/100")
                        .header("X-API-KEY", "test-key")
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje[0]").value("No encontrado"))
                .andExpect(jsonPath("$.path").value("/api/v1/products/100"));
    }

    @Test
    @DisplayName("BadRequestException se mapea a 400 con mensaje")
    void badRequestException_mappedTo400() throws Exception {
        Mockito.when(productService.buscarProductoPorId(0L)).thenThrow(new BadRequestException("ID inválido"));

        mockMvc.perform(get("/api/v1/products/0")
                        .header("X-API-KEY", "test-key")
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje[0]").value("ID inválido"))
                .andExpect(jsonPath("$.path").value("/api/v1/products/0"));
    }
}


