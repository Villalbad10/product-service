package com.product_service.service;

import com.product_service.model.Product;
import com.product_service.dto.CreateProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Interfaz de servicio para la gestión de productos.
 * Define los contratos para las operaciones CRUD y de negocio relacionadas con productos.
 * 
 * @author Diego Alexander Villalba
 * @since 1.0
 */
public interface ProductService {

    /**
     * Crea un nuevo producto en el sistema.
     * Valida que no exista un producto con el mismo nombre.
     * 
     * @param product El producto a crear
     * @return El producto creado con su ID asignado
     * @throws com.product_service.exception.ConflictException si ya existe un producto con el mismo nombre
     * @throws com.product_service.exception.BadRequestException si los datos del producto no son válidos
     */
    Product crearProducto(Product product);

    /**
     * Crea un nuevo producto en el sistema desde un DTO.
     * Valida que no exista un producto con el mismo nombre.
     * 
     * @param createProductRequest Los datos del producto a crear
     * @return El producto creado con su ID asignado
     * @throws com.product_service.exception.ConflictException si ya existe un producto con el mismo nombre
     * @throws com.product_service.exception.BadRequestException si los datos del producto no son válidos
     */
    Product crearProducto(CreateProductRequest createProductRequest);

    /**
     * Busca un producto por su ID.
     * 
     * @param id El ID del producto a buscar
     * @return Optional con el producto encontrado o vacío si no existe
     */
    Optional<Product> buscarProductoPorId(Long id);


    /**
     * Obtiene todos los productos activos (no eliminados).
     * 
     * @return Lista de productos activos
     */
    Page<Product> obtenerTodosLosProductos(Pageable pageable);


    /**
     * Actualiza un producto existente.
     * 
     * @param id El ID del producto a actualizar
     * @param productActualizado El producto con los datos actualizados
     * @return El producto actualizado
     * @throws com.product_service.exception.NotFoundException si el producto no existe
     * @throws com.product_service.exception.ConflictException si se intenta cambiar el nombre a uno ya existente
     */
    Product actualizarProducto(Long id, Product productActualizado);

    /**
     * Realiza soft delete de un producto (lo marca como eliminado).
     * 
     * @param id El ID del producto a eliminar
     * @throws com.product_service.exception.NotFoundException si el producto no existe
     */
    void eliminarProducto(Long id);

}
