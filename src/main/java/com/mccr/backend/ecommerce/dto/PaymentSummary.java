package com.mccr.backend.ecommerce.dto;

import java.time.Instant;

public record PaymentSummary(
        Long id,
        Integer amount,
        String address,
        Instant createdAt) {

}
