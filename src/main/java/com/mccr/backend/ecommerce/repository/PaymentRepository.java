package com.mccr.backend.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mccr.backend.ecommerce.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByUserId(Long userId);

    Optional<Payment> findByIdAndUserId(Long id, Long userId);

}
