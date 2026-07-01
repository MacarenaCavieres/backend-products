package com.mccr.backend.ecommerce.dto;

import java.time.Instant;
import java.util.List;
import com.mccr.backend.ecommerce.model.enums.RoleList;

public record UserResponse(
        Long id,
        String name,
        String lastname,
        String email,
        List<RoleList> roles,
        Instant createdAt,
        Instant updatedAt) {
}
