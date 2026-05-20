package com.mccr.backend.ecommerce.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mccr.backend.ecommerce.model.Product;
import com.mccr.backend.ecommerce.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public String createProduct(Product product) {

        final Product newProduct = productRepository.save(product);

        if (newProduct.getId() == null) {
            throw new RuntimeException("No se pudo crear el producto");
        }

        return "Producto creado";
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product findByProductId(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con el id " + id));
    }

    public Product updateProduct(Long id, Product updatedData) {
        final Product product = findByProductId(id);

        product.setName(updatedData.getName());
        product.setDescription(updatedData.getDescription());
        product.setUrlImage(updatedData.getUrlImage());
        product.setPrice(updatedData.getPrice());
        product.setQuantity(updatedData.getQuantity());

        productRepository.save(product);

        return product;
    }

    public String removeProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar. El producto con ID " + id + " no existe.");
        }
        productRepository.deleteById(id);

        return "Producto eliminado";
    }

}
