package com.product_service.service;

import com.product_service.dto.CreateProductRequest;
import com.product_service.exception.BadRequestException;
import com.product_service.exception.NotFoundException;
import com.product_service.model.Product;
import com.product_service.repository.ProductRepository;
import com.product_service.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Product buildValidProduct() {
        return Product.builder()
                .nombre("Laptop")
                .precio(new BigDecimal("1500.00"))
                .descripcion("Laptop para desarrollo")
                .build();
    }

    @Test
    @DisplayName("crearProducto: crea y limpia flags correctamente")
    void crearProducto_success() {
        Product toCreate = buildValidProduct();
        Product saved = Product.builder()
                .idProducto(10L)
                .nombre(toCreate.getNombre())
                .precio(toCreate.getPrecio())
                .descripcion(toCreate.getDescripcion())
                .eliminado(false)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        Product result = productService.crearProducto(toCreate);

        assertThat(result.getIdProducto()).isEqualTo(10L);
        assertThat(result.getEliminado()).isFalse();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getEliminado()).isFalse();
    }

    @Test
    @DisplayName("crearProducto: lanza BadRequest si producto es nulo")
    void crearProducto_nullProduct_throws() {
        assertThatThrownBy(() -> productService.crearProducto((Product) null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no puede ser nulo");
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("crearProducto: valida nombre requerido")
    void crearProducto_emptyName_throws() {
        Product invalid = buildValidProduct();
        invalid.setNombre(" ");
        assertThatThrownBy(() -> productService.crearProducto(invalid))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("nombre del producto es obligatorio");
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("crearProducto: valida precio positivo")
    void crearProducto_nonPositivePrice_throws() {
        Product invalid = buildValidProduct();
        invalid.setPrecio(new BigDecimal("0"));
        assertThatThrownBy(() -> productService.crearProducto(invalid))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("precio del producto debe ser mayor a cero");
        verifyNoInteractions(productRepository);
    }

    // Tests para el nuevo método que acepta CreateProductRequest

    private CreateProductRequest buildValidCreateProductRequest() {
        return CreateProductRequest.builder()
                .nombre("Laptop")
                .precio(new BigDecimal("1500.00"))
                .descripcion("Laptop para desarrollo")
                .build();
    }

    @Test
    @DisplayName("crearProducto(CreateProductRequest): crea producto correctamente desde DTO")
    void crearProductoFromDTO_success() {
        CreateProductRequest toCreate = buildValidCreateProductRequest();
        Product saved = Product.builder()
                .idProducto(10L)
                .nombre(toCreate.getNombre())
                .precio(toCreate.getPrecio())
                .descripcion(toCreate.getDescripcion())
                .eliminado(false)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        Product result = productService.crearProducto(toCreate);

        assertThat(result.getIdProducto()).isEqualTo(10L);
        assertThat(result.getNombre()).isEqualTo("Laptop");
        assertThat(result.getEliminado()).isFalse();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product capturedProduct = captor.getValue();
        assertThat(capturedProduct.getNombre()).isEqualTo("Laptop");
        assertThat(capturedProduct.getPrecio()).isEqualTo(new BigDecimal("1500.00"));
        assertThat(capturedProduct.getDescripcion()).isEqualTo("Laptop para desarrollo");
        assertThat(capturedProduct.getEliminado()).isFalse();
    }

    @Test
    @DisplayName("crearProducto(CreateProductRequest): lanza BadRequest si DTO es nulo")
    void crearProductoFromDTO_nullDTO_throws() {
        assertThatThrownBy(() -> productService.crearProducto((CreateProductRequest) null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no pueden ser nulos");
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("crearProducto(CreateProductRequest): valida nombre requerido")
    void crearProductoFromDTO_emptyName_throws() {
        CreateProductRequest invalid = buildValidCreateProductRequest();
        invalid.setNombre(" ");
        assertThatThrownBy(() -> productService.crearProducto(invalid))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("nombre del producto es obligatorio");
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("crearProducto(CreateProductRequest): valida precio positivo")
    void crearProductoFromDTO_nonPositivePrice_throws() {
        CreateProductRequest invalid = buildValidCreateProductRequest();
        invalid.setPrecio(new BigDecimal("0"));
        assertThatThrownBy(() -> productService.crearProducto(invalid))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("precio del producto debe ser mayor a cero");
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("crearProducto(CreateProductRequest): maneja descripción nula correctamente")
    void crearProductoFromDTO_nullDescription_success() {
        CreateProductRequest toCreate = CreateProductRequest.builder()
                .nombre("Mouse")
                .precio(new BigDecimal("50.00"))
                .descripcion(null)
                .build();
        Product saved = Product.builder()
                .idProducto(11L)
                .nombre("Mouse")
                .precio(new BigDecimal("50.00"))
                .descripcion(null)
                .eliminado(false)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        Product result = productService.crearProducto(toCreate);

        assertThat(result.getIdProducto()).isEqualTo(11L);
        assertThat(result.getDescripcion()).isNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("buscarProductoPorId: retorna Optional para id válido")
    void buscarProductoPorId_success() {
        Product p = buildValidProduct();
        p.setIdProducto(2L);
        when(productRepository.findById(2L)).thenReturn(Optional.of(p));

        Optional<Product> result = productService.buscarProductoPorId(2L);
        assertThat(result).isPresent();
        assertThat(result.get().getIdProducto()).isEqualTo(2L);
    }

    @Test
    @DisplayName("buscarProductoPorId: valida id positivo")
    void buscarProductoPorId_invalidId_throws() {
        assertThatThrownBy(() -> productService.buscarProductoPorId(0L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("número positivo");
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("obtenerTodosLosProductos: delega al repositorio con Pageable")
    void obtenerTodosLosProductos_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(buildValidProduct()), pageable, 1);
        when(productRepository.findAllActiveProducts(eq(pageable))).thenReturn(page);

        Page<Product> result = productService.obtenerTodosLosProductos(pageable);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(productRepository).findAllActiveProducts(pageable);
    }

    @Test
    @DisplayName("actualizarProducto: actualiza campos proporcionados")
    void actualizarProducto_success() {
        Product existing = Product.builder()
                .idProducto(5L)
                .nombre("Old")
                .precio(new BigDecimal("100.00"))
                .descripcion("desc")
                .eliminado(false)
                .build();
        when(productRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product update = Product.builder()
                .nombre("New Name")
                .precio(new BigDecimal("120.00"))
                .descripcion("nueva desc")
                .build();

        Product result = productService.actualizarProducto(5L, update);

        assertThat(result.getNombre()).isEqualTo("New Name");
        assertThat(result.getPrecio()).isEqualTo(new BigDecimal("120.00"));
        assertThat(result.getDescripcion()).isEqualTo("nueva desc");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("actualizarProducto: valida id positivo y datos presentes")
    void actualizarProducto_invalidArgs_throw() {
        assertThatThrownBy(() -> productService.actualizarProducto(0L, new Product()))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> productService.actualizarProducto(1L, null))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("actualizarProducto: lanza NotFound si no existe")
    void actualizarProducto_notFound_throw() {
        when(productRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productService.actualizarProducto(9L, new Product()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("eliminarProducto: marca eliminado=true")
    void eliminarProducto_success() {
        Product existing = Product.builder()
                .idProducto(7L)
                .nombre("Prod")
                .precio(new BigDecimal("10.00"))
                .eliminado(false)
                .build();
        when(productRepository.findById(7L)).thenReturn(Optional.of(existing));

        productService.eliminarProducto(7L);

        assertThat(existing.getEliminado()).isTrue();
        verify(productRepository).save(existing);
    }

    @Test
    @DisplayName("eliminarProducto: valida id y estados")
    void eliminarProducto_invalidCases_throw() {
        assertThatThrownBy(() -> productService.eliminarProducto(0L))
                .isInstanceOf(BadRequestException.class);

        Product deleted = Product.builder()
                .idProducto(8L)
                .nombre("X")
                .precio(new BigDecimal("10.00"))
                .eliminado(true)
                .build();
        when(productRepository.findById(8L)).thenReturn(Optional.of(deleted));

        assertThatThrownBy(() -> productService.eliminarProducto(8L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ya está eliminado");
    }
}


