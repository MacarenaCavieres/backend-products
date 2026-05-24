package com.mccr.backend.ecommerce.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class HashEncoder {
    private PasswordEncoder bcrypt = new BCryptPasswordEncoder();

    public String encode(String raw) {
        return bcrypt.encode(raw);
    }

    public boolean matches(String raw, String hashed) {
        return bcrypt.matches(raw, hashed);
    }

}
