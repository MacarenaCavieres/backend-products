package com.mccr.backend.ecommerce.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mccr.backend.ecommerce.dto.PaymentRequest;
import com.mccr.backend.ecommerce.dto.PaymentResponse;
import com.mccr.backend.ecommerce.dto.PaymentSummary;
import com.mccr.backend.ecommerce.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("payments")
    public ResponseEntity<PaymentResponse> addPayment(@RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PaymentRequest payment) {
        PaymentResponse newPayment = paymentService.createPayment(authHeader, payment);

        return ResponseEntity.status(HttpStatus.CREATED).body(newPayment);
    }

    @GetMapping("payments")
    public ResponseEntity<List<PaymentSummary>> getAll(@RequestHeader("Authorization") String authHeader) {
        List<PaymentSummary> response = paymentService.getAllPayments(authHeader);
        return ResponseEntity.ok(response);
    }

    @GetMapping("payments/{id}")
    public ResponseEntity<PaymentResponse> getPaymentDetail(@RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentDetailById(authHeader, id);
        return ResponseEntity.ok(response);
    }

}
