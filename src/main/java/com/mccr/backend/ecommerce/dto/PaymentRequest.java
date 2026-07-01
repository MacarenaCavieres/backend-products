package com.mccr.backend.ecommerce.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record PaymentRequest(
        @NotBlank(message = "El monto no puede estar vacío") String amount,

        @NotBlank(message = "La dirección no puede estar vacía") String address,

        @NotEmpty(message = "La lista de productos no puede estar vacía") @Valid List<ItemCart> items) {
}