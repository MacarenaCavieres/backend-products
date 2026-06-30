package com.mccr.backend.ecommerce.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mccr.backend.ecommerce.dto.PaymentRequest;
import com.mccr.backend.ecommerce.model.Payment;
import com.mccr.backend.ecommerce.service.PaymentService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("payments")
    public ResponseEntity<Payment> addPayment(@RequestHeader("Authorization") String authHeader,
            @RequestBody PaymentRequest payment) {
        Payment newPayment = paymentService.createPayment(authHeader, payment);

        return ResponseEntity.status(HttpStatus.CREATED).body(newPayment);
    }

    @GetMapping("payments")
    public ResponseEntity<List<Payment>> getAll() {
        List<Payment> response = paymentService.getAllPayments();
        return ResponseEntity.ok(response);
    }

}
