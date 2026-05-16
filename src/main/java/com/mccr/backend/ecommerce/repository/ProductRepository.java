package com.mccr.backend.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mccr.backend.ecommerce.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
