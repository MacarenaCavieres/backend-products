package com.mccr.backend.ecommerce.dto;

public record ProductSummaryResponse(
        Long id,
        String name,
        String urlImage,
        String brand,
        String model) {

}
