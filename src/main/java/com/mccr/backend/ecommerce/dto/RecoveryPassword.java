package com.mccr.backend.ecommerce.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;

public class RecoveryPassword {
    @Column(nullable = false)
    @NotBlank(message = "La contraseña es requerida")
    private String password;

    @Column(nullable = false)
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
