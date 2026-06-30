package com.mccr.backend.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

public class RecoveryPassword {
    @NotBlank(message = "La contraseña es requerida")
    private String password;

    @NotBlank(message = "El token es requerido")
    private String token;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
