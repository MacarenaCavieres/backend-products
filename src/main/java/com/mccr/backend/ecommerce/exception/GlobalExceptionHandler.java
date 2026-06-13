package com.mccr.backend.ecommerce.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, Object> body = new HashMap<>();

        body.put("status", ex.getStatusCode().value());
        body.put("error", ex.getStatusCode().toString());

        String message = ex.getReason() != null ? ex.getReason() : ex.getBody().getDetail();
        body.put("message", message);

        return new ResponseEntity<>(body, ex.getStatusCode());
    }
}
