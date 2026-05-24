package com.mccr.backend.ecommerce.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class HashEncoder {
    private String bcrypt = BCryptPasswordEncoder();

}
