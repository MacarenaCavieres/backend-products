package com.mccr.backend.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mccr.backend.ecommerce.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
