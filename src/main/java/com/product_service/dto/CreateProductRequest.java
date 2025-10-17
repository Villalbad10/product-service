package com.product_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para la creación de productos.
 * Contiene únicamente los campos necesarios para crear un nuevo producto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos requeridos para crear un nuevo producto")
public class CreateProductRequest {

    /**
     * Nombre del producto.
     * Campo obligatorio con validaciones de longitud.
     */
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre del producto", example = "Laptop Gaming", required = true)
    private String nombre;

    /**
     * Precio del producto.
     * Debe ser un valor positivo con precisión decimal.
     */
    @NotNull(message = "El precio del producto es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    @Schema(description = "Precio del producto", example = "1299.99", required = true)
    private BigDecimal precio;

    /**
     * Descripción detallada del producto.
     * Campo opcional con validación de longitud máxima.
     */
    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
    @Schema(description = "Descripción del producto", example = "Laptop para gaming con tarjeta gráfica RTX 4060")
    private String descripcion;
}