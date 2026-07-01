package com.mccr.backend.ecommerce.dto;

public record OrderItemResponse(
        Long id,
        Integer quantity,
        Long priceAtPurchase,
        ProductSummaryResponse product) {

}
