package com.mccr.backend.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mccr.backend.ecommerce.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

}
