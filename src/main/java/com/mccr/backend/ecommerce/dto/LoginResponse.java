package com.mccr.backend.ecommerce.dto;

import java.util.List;

import com.mccr.backend.ecommerce.model.enums.RoleList;

public record LoginResponse(
        String name,
        String lastname,
        String email,
        String token,
        List<RoleList> roles) {

}
