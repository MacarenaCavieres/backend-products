package com.mccr.backend.ecommerce.dto;

import java.util.List;

public record PaymentResponse(
        Long id,
        String amount,
        String address,
        List<OrderItemResponse> items) {
}
