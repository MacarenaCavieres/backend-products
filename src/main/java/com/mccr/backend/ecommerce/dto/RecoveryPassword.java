package com.mccr.backend.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

public record RecoveryPassword(
        @NotBlank(message = "La contraseña es requerida") String password,
        @NotBlank(message = "El token es requerido") String token) {

}
