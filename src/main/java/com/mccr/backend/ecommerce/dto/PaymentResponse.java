package com.mccr.backend.ecommerce.dto;

import java.time.Instant;
import java.util.List;

public record PaymentResponse(
		Long id,
		Integer amount,
		String address,
		Instant createdAt,
		List<OrderItemResponse> items) {
}
