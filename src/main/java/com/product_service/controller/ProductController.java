package com.product_service.controller;

import com.product_service.model.Product;
import com.product_service.dto.CreateProductRequest;
import com.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de productos.
 * Proporciona endpoints para operaciones CRUD y consultas sobre productos.
 * 
 * @author Diego Alexander Villalba
 * @since 1.0
 */
@Tag(name = "Product Controller", description = "Gestión de productos")
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * Crea un nuevo producto en el sistema.
     * 
     * @param createProductRequest Los datos del producto a crear (validado automáticamente)
     * @return ResponseEntity con el producto creado y código HTTP 201
     */
    @PostMapping("/save")
    @Operation(
        summary = "Crear un nuevo producto",
        description = "Crea un nuevo producto en el sistema. Valida que no exista un producto con el mismo nombre."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del producto inválidos o nombre duplicado"),
        @ApiResponse(responseCode = "409", description = "Conflicto: ya existe un producto con el mismo nombre"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Product> crearProducto(
            @Parameter(description = "Datos del producto a crear", required = true)
            @Valid @RequestBody CreateProductRequest createProductRequest) {
        
        log.info("Solicitud de creación de producto recibida: {}", createProductRequest.getNombre());
        
        Product productoCreado = productService.crearProducto(createProductRequest);
        
        log.info("Producto creado exitosamente con ID: {}", productoCreado.getIdProducto());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(productoCreado);
    }

    /**
     * Obtiene un producto por su ID.
     * 
     * @param id El ID del producto a buscar
     * @return ResponseEntity con el producto encontrado o 404 si no existe
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener producto por ID",
        description = "Busca y retorna un producto específico por su identificador único."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto encontrado"),
        @ApiResponse(responseCode = "400", description = "ID inválido"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Product> obtenerProductoPorId(
            @Parameter(description = "ID del producto a buscar", required = true)
            @PathVariable Long id) {
        
        log.info("Solicitud de búsqueda de producto por ID: {}", id);
        
        return productService.buscarProductoPorId(id)
                .map(producto -> {
                    log.info("Producto encontrado: {}", producto.getNombre());
                    return ResponseEntity.ok(producto);
                })
                .orElseGet(() -> {
                    log.warn("Producto no encontrado con ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Obtiene todos los productos activos de forma paginada.
     * 
     * @param pageable Parámetros de paginación (page, size, sort)
     * @return ResponseEntity con la página de productos activos
     */
    @GetMapping("/list")
    @Operation(
            summary = "Obtener todos los productos (paginado)",
            description = "Retorna una página de todos los productos activos en el sistema con soporte para paginación."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de productos obtenida exitosamente")
    })
    @Parameters({
            @Parameter(name = "page", description = "Número de página (inicia en 0)", example = "0"),
            @Parameter(name = "size", description = "Cantidad de elementos por página", example = "10"),
            @Parameter(name = "sort", description = "Ordenamiento en formato: propiedad,asc|desc. Ejemplo: idProducto,desc",
                    example = "idProducto,desc",
                    schema = @Schema(type = "string"))
    })
    public ResponseEntity<Page<Product>> obtenerTodosLosProductos(Pageable pageable) {
        log.info("Solicitud de obtención de productos paginados - página: {}, tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> productos = productService.obtenerTodosLosProductos(pageable);

        log.info("Se encontraron {} productos activos en la página {} de {}",
                productos.getContent().size(), productos.getNumber() + 1, productos.getTotalPages());

        return ResponseEntity.ok(productos);
    }


    /**
     * Actualiza un producto existente.
     * 
     * @param id El ID del producto a actualizar
     * @param productActualizado El producto con los datos actualizados
     * @return ResponseEntity con el producto actualizado o 404 si no existe
     */
    @PutMapping("/update/{id}")
    @Operation(
        summary = "Actualizar producto",
        description = "Actualiza un producto existente con los nuevos datos proporcionados."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o conflicto de nombres"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto: nombre duplicado")
    })
    public ResponseEntity<Product> actualizarProducto(
            @Parameter(description = "ID del producto a actualizar", required = true)
            @PathVariable Long id,
            @Parameter(description = "Datos actualizados del producto", required = true)
            @Valid @RequestBody Product productActualizado) {
        
        log.info("Solicitud de actualización de producto con ID: {}", id);
        
        Product productoActualizado = productService.actualizarProducto(id, productActualizado);
        
        log.info("Producto actualizado exitosamente con ID: {}", id);
        
        return ResponseEntity.ok(productoActualizado);
    }

    /**
     * Elimina un producto (soft delete).
     * 
     * @param id El ID del producto a eliminar
     * @return ResponseEntity con código HTTP 204 (No Content) si se elimina exitosamente
     */
    @DeleteMapping("/delete/{id}")
    @Operation(
        summary = "Eliminar producto",
        description = "Elimina un producto del sistema (soft delete). El producto se marca como eliminado pero no se borra físicamente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
        @ApiResponse(responseCode = "400", description = "ID inválido o producto ya eliminado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Void> eliminarProducto(
            @Parameter(description = "ID del producto a eliminar", required = true)
            @PathVariable Long id) {
        
        log.info("Solicitud de eliminación de producto con ID: {}", id);
        
        productService.eliminarProducto(id);
        
        log.info("Producto eliminado exitosamente con ID: {}", id);
        
        return ResponseEntity.noContent().build();
    }



}
