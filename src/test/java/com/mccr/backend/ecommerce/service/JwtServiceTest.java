package com.mccr.backend.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.mccr.backend.ecommerce.model.Role;
import com.mccr.backend.ecommerce.model.User;
import com.mccr.backend.ecommerce.model.enums.RoleList;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        String testSecret = Base64.getEncoder()
                .encodeToString("12345678901234567890123456789012".getBytes());

        jwtService = new JwtService(testSecret);
    }

    @Test
    @DisplayName("It should generate a valid JWT token")
    void shouldGenerateAValidJWT() {
        User user = buildUser();
        user.setId(1L);

        Role role = buildRole();

        String token = jwtService.generateAccessToken(user.getId().toString(), List.of(role));
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<?> roles = claims.get("role", List.class);

        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();

        long difference = expiration.getTime() - issuedAt.getTime();

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertFalse(roles.isEmpty());
        assertEquals(jwtService.expirationTokenTime, difference);
        assertEquals("1", claims.getSubject());
    }

    @Test
    @DisplayName("It should generate a valid JWT token")
    void shouldGenerateAValidJWTInGenerateResetPasswordToken() {
        User user = buildUser();
        user.setId(1L);

        Role role = buildRole();

        String token = jwtService.generateResetPasswordToken(user.getId().toString(), List.of(role));
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<?> roles = claims.get("role", List.class);

        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();

        long difference = expiration.getTime() - issuedAt.getTime();

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertFalse(roles.isEmpty());
        assertEquals(jwtService.expirationTokenResetTime, difference);
        assertEquals("1", claims.getSubject());
    }

    @Test
    @DisplayName("It should return true when the token is valid and not expired")
    void shouldReturnTrueWhenTokenIsValidAndNotExpired() {
        User user = buildUser();
        user.setId(1L);

        Role role = buildRole();

        String token = jwtService.generateResetPasswordToken(user.getId().toString(), List.of(role));
        boolean isValid = jwtService.validateAcessToken(token);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("It should return false when the token is null")
    void shouldReturnFalseWhenTokenIsNull() {
        String token = null;
        boolean isValid = jwtService.validateAcessToken(token);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("It should return false when the token is empty")
    void shouldReturnFalseWhenTokenIsEmpty() {

        boolean isValid = jwtService.validateAcessToken("");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("It should return false when the token is invalid")
    void shouldReturnFalseWhenTokenIsInvalid() {

        boolean isValid = jwtService.validateAcessToken("sdfyhgsudb834ur");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("It should return false when the token is expired")
    void shouldReturnFalseWhenTokenIsExpired() {
        User user = buildUser();
        user.setId(1L);
        Role role = buildRole();

        jwtService.expirationTokenTime = -1000L;

        String token = jwtService.generateAccessToken(user.getId().toString(), List.of(role));
        boolean result = jwtService.validateAcessToken(token);

        assertFalse(result);
    }

    @Test
    @DisplayName("It should return the user ID contained in the token")
    void shouldReturnTheUserIdInTheToken() {
        User user = buildUser();
        user.setId(1L);
        Role role = buildRole();

        String token = jwtService.generateAccessToken(user.getId().toString(), List.of(role));
        String id = jwtService.getUserIdFromToken(token);

        assertEquals(user.getId().toString(), id);

    }

    @Test
    @DisplayName("It should accept a token prefixed with 'Bearer '")
    void shouldAcceptTokenPrefixedWithBearer() {
        User user = buildUser();
        user.setId(1L);
        Role role = buildRole();

        String token = jwtService.generateAccessToken(user.getId().toString(), List.of(role));
        String id = jwtService.getUserIdFromToken("Bearer " + token);

        assertEquals(user.getId().toString(), id);

    }

    @Test
    @DisplayName("It should throw a ResponseStatusException when the token is invalid")
    void shouldThrowExceptionWhenTokenIsInvalid() {

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jwtService.getUserIdFromToken(""));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("No autorizado", ex.getReason());

    }

    @Test
    @DisplayName("It should return the list of roles when the token contains multiple roles")
    void shouldReturnListOfRolesWhenTokenContainsMultipleRoles() {
        User user = buildUser();
        user.setId(1L);
        List<Role> roles = new ArrayList<>();

        Role supervisorRole = new Role();
        supervisorRole.setId(2L);
        supervisorRole.setRole(RoleList.ROLE_SUPERVISOR);

        roles.add(buildRole());
        roles.add(supervisorRole);
        String token = jwtService.generateAccessToken(user.getId().toString(), roles);
        List<RoleList> rolesList = jwtService.getRolesFromToken(token);

        assertEquals(RoleList.ROLE_USER, rolesList.get(0));
        assertEquals(RoleList.ROLE_SUPERVISOR, rolesList.get(1));
        assertEquals(2, rolesList.size());

    }

    @Test
    @DisplayName("It should return the list of roles when the token contains a single role")
    void shouldReturnListOfRolesWhenTokenContainsSingleRole() {
        User user = buildUser();
        user.setId(1L);
        List<Role> roles = new ArrayList<>();

        roles.add(buildRole());

        String token = jwtService.generateAccessToken(user.getId().toString(), roles);
        List<RoleList> rolesList = jwtService.getRolesFromToken(token);

        assertEquals(RoleList.ROLE_USER, rolesList.get(0));
        assertEquals(1, rolesList.size());

    }

    @Test
    @DisplayName("It should return an empty list when the token does not contain the 'role' claim")
    void shouldReturnAnEmptyListWhenTokenNotContainsRoleClaim() {
        User user = buildUser();
        user.setId(1L);

        String token = Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 19_600_000L)).signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
        List<RoleList> rolesList = jwtService.getRolesFromToken(token);

        assertEquals(0, rolesList.size());

    }

    @Test
    @DisplayName("It should throw a ResponseStatusException when the token is invalid")
    void shouldThrowExceptionWhenInvalidTokenInGetRolesFromToken() {

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jwtService.getRolesFromToken(""));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("No autorizado", ex.getReason());

    }

    private User buildUser() {
        User user = new User();
        user.setName("Alexandra");
        user.setLastname("Trikru");
        user.setEmail("lexa@mail.com");
        user.setPassword("12345678");

        return user;
    }

    private Role buildRole() {
        Role role = new Role();
        role.setId(1L);
        role.setRole(RoleList.ROLE_USER);

        return role;
    }

    private SecretKey getSecretKey() {
        String testSecret = Base64.getEncoder()
                .encodeToString("12345678901234567890123456789012".getBytes());

        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(testSecret));
    }

}
