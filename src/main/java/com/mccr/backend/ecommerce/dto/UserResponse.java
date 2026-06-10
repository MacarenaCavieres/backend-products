package com.mccr.backend.ecommerce.dto;

import java.time.Instant;
import java.util.List;

import com.mccr.backend.ecommerce.model.enums.RoleList;

public class UserResponse {

    private Long id;
    private String name;
    private String lastname;
    private String email;
    private List<RoleList> roles;
    private Instant createdAt;
    private Instant updatedAt;

    public UserResponse(Long id, String name, String lastname, String email, List<RoleList> roles, Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.roles = roles;
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

    public List<RoleList> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleList> roles) {
        this.roles = roles;
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
