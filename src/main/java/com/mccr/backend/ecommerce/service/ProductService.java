package com.mccr.backend.ecommerce.service;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mccr.backend.ecommerce.model.Product;
import com.mccr.backend.ecommerce.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /*
     * Tests:
     * Debe guardar correctamente un producto y devolverlo
     * Debe lanzar ResponseStatusException cuando el repositorio devuelve un
     * producto sin id
     */
    public Product createProduct(Product product) {

        final Product newProduct = productRepository.save(product);

        if (newProduct.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Error: No se pudo crear el producto");
        }

        return newProduct;
    }

    /*
     * Tests:
     * Debe devolver todos los productos existentes.
     * Debe devolver una lista vacía cuando no existen productos.
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /*
     * Tests:
     * Debe devolver el producto cuando existe.
     * Debe lanzar ResponseStatusException cuando el producto no existe.
     */
    public Product findByProductId(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Error: Producto no encontrado"));
    }

    /*
     * Tests:
     * Debe actualizar correctamente todos los campos del producto.
     * Debe actualizar la fecha updatedAt.
     * Debe lanzar ResponseStatusException cuando el producto no existe.
     * Debe guardar el producto actualizado en el repositorio.
     */
    public Product updateProduct(Long id, Product updatedData) {
        final Product product = findByProductId(id);

        product.setName(updatedData.getName());
        product.setDescription(updatedData.getDescription());
        product.setUrlImage(updatedData.getUrlImage());
        product.setPrice(updatedData.getPrice());
        product.setQuantity(updatedData.getQuantity());
        product.setBrand(updatedData.getBrand());
        product.setModel(updatedData.getModel());
        product.setOrigin(updatedData.getOrigin());
        product.setUsefulLife(updatedData.getUsefulLife());
        product.setUpdatedAt(Instant.now());

        productRepository.save(product);

        return product;
    }

    /*
     * Tests:
     * Debe eliminar el producto cuando existe.
     * Debe lanzar ResponseStatusException cuando el producto no existe.
     * Debe llamar a deleteById().
     */
    public String removeProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Error: Producto no encontrado");
        }
        productRepository.deleteById(id);

        return "Producto eliminado";
    }

}
