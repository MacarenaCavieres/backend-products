package com.mccr.backend.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mccr.backend.ecommerce.model.PasswordResetToken;
import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordResetToken, Long> {
    public void deleteByUserId(Long userId);

    Optional<PasswordResetToken> findByToken(String token);

}
