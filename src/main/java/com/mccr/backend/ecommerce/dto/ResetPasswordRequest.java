package com.mccr.backend.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "El email es obligatorio") @Email(message = "El formato del email no es válido") String email) {
}
