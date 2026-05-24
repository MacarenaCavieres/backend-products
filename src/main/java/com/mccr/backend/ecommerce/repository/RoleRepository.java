package com.mccr.backend.ecommerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mccr.backend.ecommerce.model.Role;
import com.mccr.backend.ecommerce.model.enums.RoleList;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRole(RoleList roleName);

}
