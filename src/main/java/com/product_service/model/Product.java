package com.product_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Product que representa un producto en el sistema.
 * Implementa soft delete para mantener la integridad referencial.
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "eliminado = false")
public class Product {

    /**
     * Identificador único del producto.
     * Se genera automáticamente usando una secuencia.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @Column(name = "id_producto", nullable = false, unique = true)
    private Long idProducto;

    /**
     * Nombre del producto.
     * Campo obligatorio con validaciones de longitud.
     */
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * Precio del producto.
     * Debe ser un valor positivo con precisión decimal.
     */
    @NotNull(message = "El precio del producto es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    /**
     * Descripción detallada del producto.
     * Campo opcional con validación de longitud máxima.
     */
    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    /**
     * Campo para implementar soft delete.
     * Por defecto es false (no eliminado).
     */
    @Builder.Default
    @Column(name = "eliminado", nullable = false)
    private Boolean eliminado = false;

    /**
     * Timestamp de creación del registro.
     * Se establece automáticamente al crear el producto.
     */
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Timestamp de última modificación.
     * Se actualiza automáticamente al modificar el producto.
     */
    @UpdateTimestamp
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
}
