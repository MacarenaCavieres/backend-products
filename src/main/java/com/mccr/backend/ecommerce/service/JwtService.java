package com.mccr.backend.ecommerce.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mccr.backend.ecommerce.model.Role;
import com.mccr.backend.ecommerce.model.enums.RoleList;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey secretKey;
    public Long expirationTokenTime = 19_600_000L;
    public Long expirationTokenResetTime = 900_000L;

    public JwtService(@Value("${jwt.secret}") String jwtSecret) {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret));
    }

    /*
     * Tests:
     * Debe generar un token JWT válido.
     * Debe incluir el id del usuario como subject.
     * Debe incluir los roles del usuario en el token.
     * Debe generar un token con la fecha de expiración configurada.
     */
    public String generateAccessToken(String userId, List<Role> role) {
        return generateToken(userId, role, expirationTokenTime);
    }

    /*
     * Tests:
     * Debe generar un token JWT válido.
     * Debe incluir el id del usuario como subject.
     * Debe incluir los roles del usuario en el token.
     * Debe generar un token con el tiempo de expiración para recuperación de
     * contraseña.
     */
    public String generateResetPasswordToken(String userId, List<Role> role) {
        return generateToken(userId, role, expirationTokenResetTime);
    }

    /*
     * Tests:
     * Debe retornar true cuando el token es válido y no está expirado.
     * Debe retornar false cuando el token es null.
     * Debe retornar false cuando el token está vacío.
     * Debe retornar false cuando el token es inválido.
     * Debe retornar false cuando el token está expirado.
     */
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

    /*
     * Tests:
     * Debe devolver el id del usuario contenido en el token.
     * Debe aceptar un token con prefijo "Bearer ".
     * Debe aceptar un token sin el prefijo "Bearer ".
     * Debe lanzar ResponseStatusException cuando el token es inválido.
     */
    public String getUserIdFromToken(String token) {
        Claims claims = parseAllClaims(token);
        if (claims == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");
        }
        return claims.getSubject();
    }

    /*
     * Tests:
     * Debe devolver la lista de roles cuando el token contiene múltiples roles.
     * Debe devolver la lista de roles cuando el token contiene un único rol.
     * Debe devolver una lista vacía cuando el token no contiene el claim "role".
     * Debe lanzar ResponseStatusException cuando el token es inválido.
     */
    public List<RoleList> getRolesFromToken(String token) {
        Claims claims = parseAllClaims(token);
        if (claims == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");
        }

        List<RoleList> rolesNames = new ArrayList<>();
        Object roleClaim = claims.get("role");

        if (roleClaim instanceof List) {
            List<?> rolesList = (List<?>) roleClaim;

            for (Object roleObj : rolesList) {
                if (roleObj instanceof Map) {
                    Map<?, ?> roleMap = (Map<?, ?>) roleObj;
                    String roleStr = roleMap.get("role").toString();
                    rolesNames.add(RoleList.valueOf(roleStr));
                } else if (roleObj != null) {
                    rolesNames.add(RoleList.valueOf(roleObj.toString()));
                }
            }
            return rolesNames;
        }

        if (roleClaim instanceof String) {
            rolesNames.add(RoleList.valueOf((String) roleClaim));
            return rolesNames;
        }

        return rolesNames;
    }

    private String generateToken(String userId, List<Role> role, Long expiration) {
        return Jwts.builder()
                .claim("role", role)
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration)).signWith(secretKey, Jwts.SIG.HS256)
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
