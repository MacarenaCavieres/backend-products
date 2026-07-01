package com.mccr.backend.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemCart(
        @NotNull(message = "El ID del producto es obligatorio") Long productId,

        @NotNull(message = "La cantidad es obligatoria") @Positive(message = "La cantidad debe ser mayor a cero") Integer quantity,

        @NotNull(message = "El precio de compra es obligatorio") @Positive(message = "El precio debe ser un valor positivo") Long priceAtPurchase) {
}
