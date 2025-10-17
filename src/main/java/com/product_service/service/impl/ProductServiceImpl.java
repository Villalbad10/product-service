package com.product_service.service.impl;

import com.product_service.exception.BadRequestException;
import com.product_service.exception.NotFoundException;
import com.product_service.model.Product;
import com.product_service.dto.CreateProductRequest;
import com.product_service.repository.ProductRepository;
import com.product_service.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Implementación del servicio para la gestión de productos.
 * Maneja la lógica de negocio y las transacciones relacionadas con productos.
 * 
 * @author Diego Alexander Villalba
 * @since 1.0
 */
@Slf4j
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Crea un nuevo producto en el sistema.
     * Realiza validaciones de negocio y guarda el producto en la base de datos.
     * 
     * @param product El producto a crear con sus datos básicos
     * @return El producto creado con su ID asignado por la base de datos
     * @throws BadRequestException si el producto es nulo, nombre vacío o precio inválido
     * @throws Exception si ocurre un error durante el guardado
     */
    @Override
    @Transactional
    public Product crearProducto(Product product) {
        //log.info("Iniciando creación de producto: {}", product.getNombre());

        // Validar que el producto no sea nulo
        if (product == null) {
            throw new BadRequestException("El producto no puede ser nulo");
        }

        // Validar que el nombre no sea nulo o vacío
        if (product.getNombre() == null || product.getNombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre del producto es obligatorio");
        }

        // Validar que el precio no sea nulo y sea positivo
        if (product.getPrecio() == null || product.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El precio del producto debe ser mayor a cero");
        }

        try {
            // Asegurar que el producto no esté marcado como eliminado al crearlo
            product.setEliminado(false);
            
            Product productoCreado = productRepository.save(product);
            log.info("Producto creado exitosamente con ID: {}", productoCreado.getIdProducto());
            
            return productoCreado;
        } catch (Exception e) {
            log.error("Error al crear el producto: {}", e.getMessage(), e);
            throw new BadRequestException("Error interno al crear el producto: " + e.getMessage());
        }
    }

    /**
     * Crea un nuevo producto en el sistema desde un DTO.
     * Realiza validaciones de negocio y guarda el producto en la base de datos.
     * 
     * @param createProductRequest Los datos del producto a crear
     * @return El producto creado con su ID asignado por la base de datos
     * @throws BadRequestException si los datos del producto no son válidos
     * @throws Exception si ocurre un error durante el guardado
     */
    @Override
    @Transactional
    public Product crearProducto(CreateProductRequest createProductRequest) {
        // Validar que el DTO no sea nulo
        if (createProductRequest == null) {
            throw new BadRequestException("Los datos del producto no pueden ser nulos");
        }

        log.info("Iniciando creación de producto desde DTO: {}", createProductRequest.getNombre());

        // Validar que el nombre no sea nulo o vacío
        if (createProductRequest.getNombre() == null || createProductRequest.getNombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre del producto es obligatorio");
        }

        // Validar que el precio no sea nulo y sea positivo
        if (createProductRequest.getPrecio() == null || createProductRequest.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El precio del producto debe ser mayor a cero");
        }

        try {
            // Crear el producto desde el DTO
            Product product = Product.builder()
                    .nombre(createProductRequest.getNombre().trim())
                    .precio(createProductRequest.getPrecio())
                    .descripcion(createProductRequest.getDescripcion() != null ? 
                               createProductRequest.getDescripcion().trim() : null)
                    .eliminado(false)
                    .build();
            
            Product productoCreado = productRepository.save(product);
            log.info("Producto creado exitosamente desde DTO con ID: {}", productoCreado.getIdProducto());
            
            return productoCreado;
        } catch (Exception e) {
            log.error("Error al crear el producto desde DTO: {}", e.getMessage(), e);
            throw new BadRequestException("Error interno al crear el producto: " + e.getMessage());
        }
    }

    /**
     * Busca un producto por su identificador único.
     * Realiza validación del ID y consulta la base de datos.
     * 
     * @param id El ID del producto a buscar
     * @return Optional conteniendo el producto si existe, vacío si no se encuentra
     * @throws BadRequestException si el ID es nulo, cero o negativo
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Product> buscarProductoPorId(Long id) {
        log.debug("Buscando producto por ID: {}", id);
        
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del producto debe ser un número positivo");
        }
        
        return productRepository.findById(id);
    }


    /**
     * Obtiene todos los productos activos de forma paginada.
     * Filtra automáticamente los productos eliminados (soft delete).
     * 
     * @param pageable Parámetros de paginación (número de página, tamaño, ordenamiento)
     * @return Página de productos activos con metadatos de paginación
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Product> obtenerTodosLosProductos(Pageable pageable) {
        log.debug("Obteniendo todos los productos activos con paginación - página: {}, tamaño: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findAllActiveProducts(pageable);
    }



    /**
     * Actualiza un producto existente con nuevos datos.
     * Valida que el producto existe y actualiza solo los campos proporcionados.
     * 
     * @param id El ID del producto a actualizar
     * @param productActualizado El producto con los nuevos datos a aplicar
     * @return El producto actualizado y guardado en la base de datos
     * @throws BadRequestException si el ID es inválido o los datos de actualización son nulos
     * @throws NotFoundException si no existe un producto con el ID especificado
     * @throws BadRequestException si los datos validados son inválidos (nombre vacío, precio negativo)
     */
    @Override
    @Transactional
    public Product actualizarProducto(Long id, Product productActualizado) {
        log.info("Actualizando producto con ID: {}", id);
        
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del producto debe ser un número positivo");
        }
        
        if (productActualizado == null) {
            throw new BadRequestException("Los datos del producto a actualizar son obligatorios");
        }

        // Buscar el producto existente
        Product productoExistente = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontró un producto con ID: " + id));

        // Validar que el nombre no esté vacío si se proporciona
        if (productActualizado.getNombre() != null && productActualizado.getNombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre del producto no puede estar vacío");
        }

        // Validar que el precio sea positivo si se proporciona
        if (productActualizado.getPrecio() != null && productActualizado.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El precio del producto debe ser mayor a cero");
        }


        try {
            // Actualizar solo los campos proporcionados
            if (productActualizado.getNombre() != null) {
                productoExistente.setNombre(productActualizado.getNombre().trim());
            }
            
            if (productActualizado.getPrecio() != null) {
                productoExistente.setPrecio(productActualizado.getPrecio());
            }
            
            if (productActualizado.getDescripcion() != null) {
                productoExistente.setDescripcion(productActualizado.getDescripcion().trim());
            }

            Product productoActualizadoGuardado = productRepository.save(productoExistente);
            log.info("Producto actualizado exitosamente con ID: {}", id);
            
            return productoActualizadoGuardado;
        } catch (Exception e) {
            log.error("Error al actualizar el producto con ID {}: {}", id, e.getMessage(), e);
            throw new BadRequestException("Error interno al actualizar el producto: " + e.getMessage());
        }
    }

    /**
     * Elimina un producto del sistema mediante soft delete.
     * Marca el producto como eliminado sin borrarlo físicamente de la base de datos.
     * 
     * @param id El ID del producto a eliminar
     * @throws BadRequestException si el ID es inválido o el producto ya está eliminado
     * @throws NotFoundException si no existe un producto con el ID especificado
     * @throws Exception si ocurre un error durante la actualización del estado
     */
    @Override
    @Transactional
    public void eliminarProducto(Long id) {
        log.info("Eliminando producto con ID: {}", id);
        
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del producto debe ser un número positivo");
        }

        Product producto = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontró un producto con ID: " + id));

        if (producto.getEliminado()) {
            throw new BadRequestException("El producto ya está eliminado");
        }

        try {
            producto.setEliminado(true);
            productRepository.save(producto);
            log.info("Producto eliminado exitosamente con ID: {}", id);
        } catch (Exception e) {
            log.error("Error al eliminar el producto con ID {}: {}", id, e.getMessage(), e);
            throw new BadRequestException("Error interno al eliminar el producto: " + e.getMessage());
        }
    }


}
