package com.mccr.backend.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.mccr.backend.ecommerce.model.Product;
import com.mccr.backend.ecommerce.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("Should create and return the product")
    void shouldCreateAndReturnProduct() {
        // Arrange
        Product product = buildProduct();

        Product savedProduct = buildProduct();
        savedProduct.setId(1L);

        when(productRepository.save(product)).thenReturn(savedProduct);

        // Act
        Product result = productService.createProduct(product);

        // Assert
        assertEquals(savedProduct, result);
        verify(productRepository).save(product);

    }

    @Test
    @DisplayName("Should return ResponseStatusException")
    void shouldThrowException() {
        Product product = buildProduct();
        when(productRepository.save(product)).thenReturn(product);
        assertThrows(ResponseStatusException.class, () -> productService.createProduct(product));
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should return BAD_REQUEST status")
    void shouldThrowBadRequestStatus() {
        Product product = buildProduct();
        when(productRepository.save(product)).thenReturn(product);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.createProduct(product));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    @DisplayName("Should return all products")
    void shouldReturnAllProducts() {
        Product product1 = buildProduct();
        product1.setId(1L);
        Product product2 = buildProduct();
        product2.setId(2L);
        Product product3 = buildProduct();
        product3.setId(3L);

        List<Product> products = new ArrayList<>();
        products.add(product1);
        products.add(product2);
        products.add(product3);

        when(productRepository.findAll()).thenReturn(products);

        List<Product> productsService = productService.getAllProducts();

        assertEquals(products, productsService);
        assertEquals(3, productsService.size());
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("Should return and empty list")
    void shouldReturnEmptyList() {
        List<Product> products = new ArrayList<>();

        when(productRepository.findAll()).thenReturn(products);

        List<Product> productsService = productService.getAllProducts();

        assertEquals(products, productsService);
        assertEquals(0, productsService.size());
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("Should return the product that matches with id")
    void shouldReturnProductById() {
        Product product = buildProduct();
        product.setId(1L);
        Optional<Product> optionalProduct = Optional.of(product);

        when(productRepository.findById(1L)).thenReturn(optionalProduct);

        Product productFounded = productService.findByProductId(1L);

        assertEquals(optionalProduct.get(), productFounded);
        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return NOT_FOUND status when doesn't exist")
    void shouldThrowExceptionByProductId() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.findByProductId(1L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(productRepository).findById(1L);
    }

    /*
     * Tests:
     * Debe actualizar correctamente todos los campos del producto.
     * Debe actualizar la fecha updatedAt.
     * Debe lanzar ResponseStatusException cuando el producto no existe.
     * Debe guardar el producto actualizado en el repositorio.
     */

    @Test
    @DisplayName("Should update all product fields")
    void shouldUpdateProduct() {
        Product existingProduct = buildProduct();
        existingProduct.setId(1L);
        existingProduct.setUpdatedAt(Instant.now());

        Product newData = new Product();

        newData.setName("Cámara Mirrorless Canon EOS R50 + Lente 18-45mm IS STM");
        newData.setDescription(
                "El sensor APS-C de gran tamaño proporciona una calidad de imagen increíble y control sobre la profundidad de campo. Perfecciona un aspecto profesional. Selecciona el modo Escena nocturna sin trípode para tomar varias imágenes y combinarlas automáticamente en una imagen más clara.");
        newData.setUrlImage(
                "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?fm=jpg&q=60&w=3000&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D");
        newData.setPrice(789990L);
        newData.setQuantity(10L);
        newData.setBrand("Canon");
        newData.setModel("EOS R50 18-45 IS STM");
        newData.setOrigin("Vietnam");
        newData.setUsefulLife("5 años");

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);

        Product result = productService.updateProduct(1L, newData);

        assertEquals("Cámara Mirrorless Canon EOS R50 + Lente 18-45mm IS STM", result.getName());

        assertEquals(
                "El sensor APS-C de gran tamaño proporciona una calidad de imagen increíble y control sobre la profundidad de campo. Perfecciona un aspecto profesional. Selecciona el modo Escena nocturna sin trípode para tomar varias imágenes y combinarlas automáticamente en una imagen más clara.",
                result.getDescription());
        assertEquals(
                "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?fm=jpg&q=60&w=3000&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                result.getUrlImage());
        assertEquals(789990L, result.getPrice());
        assertEquals(10L, result.getQuantity());
        assertEquals("Canon", result.getBrand());
        assertEquals("EOS R50 18-45 IS STM", result.getModel());
        assertEquals("Vietnam", result.getOrigin());
        assertEquals("5 años", result.getUsefulLife());
        assertEquals(existingProduct.getUpdatedAt(), result.getUpdatedAt());
        verify(productRepository).findById(1L);
        verify(productRepository).save(existingProduct);
    }

    @Test
    @DisplayName("Should throw exception when the product in updateProduct doesn't exist")
    void shouldThrowExceptionMissingProductId() {
        Product prod = buildProduct();

        when(productRepository.findById(1L)).thenReturn(Optional.of(prod));
    }

    private Product buildProduct() {
        Product product = new Product();
        product.setName("Cámara Mirrorless Canon EOS R50 + Lente 18-45mm IS STM");
        product.setDescription(
                "El sensor APS-C de gran tamaño proporciona una calidad de imagen increíble y control sobre la profundidad de campo. Perfecciona un aspecto profesional. Selecciona el modo Escena nocturna sin trípode para tomar varias imágenes y combinarlas automáticamente en una imagen más clara.");
        product.setUrlImage(
                "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?fm=jpg&q=60&w=3000&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D");
        product.setPrice(749990L);
        product.setQuantity(10L);
        product.setBrand("Canon");
        product.setModel("EOS R50 18-45 IS STM");
        product.setOrigin("Vietnam");
        product.setUsefulLife("5 años");

        return product;
    }

}
