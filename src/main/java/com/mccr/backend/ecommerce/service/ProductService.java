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

    public Product createProduct(Product product) {

        final Product newProduct = productRepository.save(product);

        if (newProduct.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Error: No se pudo crear el producto");
        }

        return newProduct;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product findByProductId(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Error: Producto no encontrado"));
    }

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

    public String removeProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Error: Producto no encontrado");
        }
        productRepository.deleteById(id);

        return "Producto eliminado";
    }

}
