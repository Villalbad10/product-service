package com.product_service.repository;

import com.product_service.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Product.
 * Extiende JpaRepository para obtener operaciones CRUD básicas.
 * 
 * @author Diego Alexander Villalba
 * @since 1.0
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Obtiene todos los productos activos (no eliminados) de forma paginada.
     * 
     * @param pageable Parámetros de paginación
     * @return Página de productos activos
     */
    @Query("SELECT p FROM Product p WHERE p.eliminado = false")
    Page<Product> findAllActiveProducts(Pageable pageable);

}
