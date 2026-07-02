package com.mccr.backend.ecommerce.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
		@NotNull(message = "El monto no puede ser nulo") @Min(value = 1, message = "El monto debe ser mayor o igual a 1") Integer amount,

		@NotBlank(message = "La dirección no puede estar vacía") String address,

		@NotEmpty(message = "La lista de productos no puede estar vacía") @Valid List<ItemCart> items) {
}