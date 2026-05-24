package com.mccr.backend.ecommerce.service;

import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mccr.backend.ecommerce.model.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String jwtSecret) {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret));
    }

    public String generateAccessToken(String userId, List<Role> role) {
        return generateToken(userId, role);
    }

    public boolean validateAcessToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        Claims claims = parseAllClaims(token);

        if (claims == null) {
            return false;
        }

        Date expiration = claims.getExpiration();
        return expiration.after(new Date());
    }

    public String getUserIdFromToken(String token) {
        Claims claims = parseAllClaims(token);
        if (claims == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");
        }
        return claims.getSubject();
    }

    public String getRoleFromToken(String token) {
        Claims claims = parseAllClaims(token);
        if (claims == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");
        }
        return claims.get("role", String.class);
    }

    private String generateToken(String userId, List<Role> role) {
        return Jwts.builder()
                .claim("role", role)
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000)).signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    private Claims parseAllClaims(String token) {
        String rawToken = "";

        if (token.startsWith("Bearer")) {
            rawToken = token.replace("Bearer ", "");
        } else {
            rawToken = token;
        }

        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(rawToken).getPayload();

        } catch (Exception e) {
            System.out.println("error " + e.getMessage());
            return null;
        }
    }

}
