package com.mccr.backend.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mccr.backend.ecommerce.model.User;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByEmail(String email);

}
