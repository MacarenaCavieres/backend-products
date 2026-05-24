package com.mccr.backend.ecommerce.dto;

import java.time.Instant;

public class RegisterResponse {

    private Long id;
    private String name;
    private String lastname;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;

    public RegisterResponse(Long id, String name, String lastname, String email, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

}
