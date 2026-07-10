package com.mccr.backend.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.mccr.backend.ecommerce.config.HashEncoder;
import com.mccr.backend.ecommerce.dto.LoginRequest;
import com.mccr.backend.ecommerce.dto.LoginResponse;
import com.mccr.backend.ecommerce.dto.UserResponse;
import com.mccr.backend.ecommerce.model.Role;
import com.mccr.backend.ecommerce.model.User;
import com.mccr.backend.ecommerce.model.enums.RoleList;
import com.mccr.backend.ecommerce.repository.RoleRepository;
import com.mccr.backend.ecommerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private HashEncoder hashEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should hash the pass before stores it")
    void shouldHashThePass() {
        User u = buildUser();
        Role role = buildRole();

        when(userRepository.findByEmail("lexa@mail.com")).thenReturn(Optional.empty());
        when(userRepository.save(u)).thenReturn(u);
        when(hashEncoder.encode("12345678")).thenReturn("82374y2hf238hfr2hf2478932hfn");
        when(roleRepository.findByRole(RoleList.ROLE_USER)).thenReturn(Optional.of(role));

        userService.register(u);

        assertNotEquals("12345678", u.getPassword());
        assertEquals("82374y2hf238hfr2hf2478932hfn", u.getPassword());

        verify(userRepository).findByEmail("lexa@mail.com");
        verify(hashEncoder).encode("12345678");
        verify(roleRepository).findByRole(RoleList.ROLE_USER);
        verify(userRepository).save(u);
    }

    @Test
    @DisplayName("It should throw a ResponseStatusException when the email is already registered")
    void shouldThrowExceptionForEmailRegistered() {
        User u = buildUser();

        when(userRepository.findByEmail("lexa@mail.com")).thenReturn(Optional.of(u));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.register(u));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Error: Usuario ya registrado", ex.getReason());

        verify(userRepository).findByEmail("lexa@mail.com");
    }

    @Test
    @DisplayName("It should throw a ResponseStatusException when the ROLE_USER role does not exist")
    void shouldThrowExceptionForNotFoundRole() {
        User u = buildUser();

        when(userRepository.findByEmail("lexa@mail.com")).thenReturn(Optional.empty());
        when(roleRepository.findByRole(RoleList.ROLE_USER)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.register(u));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
        assertEquals("Error: El rol ROLE_USER no existe en la base de datos", ex.getReason());

        verify(userRepository).findByEmail("lexa@mail.com");
        verify(roleRepository).findByRole(RoleList.ROLE_USER);

    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("It should assign the ROLE_USER role to the new user")
    void shouldAssignRoleUserToUser() {
        User u = buildUser();
        Role r = buildRole();

        when(userRepository.findByEmail("lexa@mail.com")).thenReturn(Optional.empty());
        when(roleRepository.findByRole(RoleList.ROLE_USER)).thenReturn(Optional.of(r));
        when(userRepository.save(u)).thenReturn(u);
        when(hashEncoder.encode("12345678")).thenReturn("82374y2hf238hfr2hf2478932hfn");

        UserResponse result = userService.register(u);
        List<RoleList> roles = u.getRoles().stream().map(Role::getRole).toList();

        assertEquals(roles, result.roles());

        verify(userRepository).findByEmail("lexa@mail.com");
        verify(hashEncoder).encode("12345678");
        verify(roleRepository).findByRole(RoleList.ROLE_USER);
        verify(userRepository).save(u);

    }

    @Test
    @DisplayName("It should correctly save the user in the repository")
    void shouldCreateTheUser() {
        User u = buildUser();
        Role r = buildRole();

        when(userRepository.findByEmail("lexa@mail.com")).thenReturn(Optional.empty());
        when(roleRepository.findByRole(RoleList.ROLE_USER)).thenReturn(Optional.of(r));
        when(userRepository.save(u)).thenReturn(u);
        when(hashEncoder.encode("12345678")).thenReturn("82374y2hf238hfr2hf2478932hfn");

        UserResponse result = userService.register(u);

        assertEquals("Alexandra", result.name());
        assertEquals("Trikru", result.lastname());
        assertEquals("lexa@mail.com", result.email());

        verify(userRepository).findByEmail("lexa@mail.com");
        verify(hashEncoder).encode("12345678");
        verify(roleRepository).findByRole(RoleList.ROLE_USER);
        verify(userRepository).save(u);

    }

    @Test
    @DisplayName("It should throw a ResponseStatusException when the user does not exist.")
    void shouldThrowExceptionNotFoundUser() {
        LoginRequest request = new LoginRequest("lexa@mail.com", "12345678");

        when(userRepository.findByEmail("lexa@mail.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.login(request));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Error: Usuario no registrado", ex.getReason());

        verify(userRepository).findByEmail("lexa@mail.com");

    }

    @Test
    @DisplayName("It should throw a ResponseStatusException when the password is incorrect")
    void shouldThrowExceptionIncorrectPassword() {
        User u = buildUser();
        LoginRequest request = new LoginRequest("lexa@mail.com", "12345672");

        when(userRepository.findByEmail("lexa@mail.com")).thenReturn(Optional.of(u));
        when(hashEncoder.matches(request.password(), u.getPassword())).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.login(request));

        assertEquals(HttpStatus.NOT_ACCEPTABLE, ex.getStatusCode());
        assertEquals("Error: Credenciales inválidas", ex.getReason());

        verify(userRepository).findByEmail("lexa@mail.com");
        verify(hashEncoder).matches(request.password(), u.getPassword());

    }

    @Test
    @DisplayName("It should correctly generate the JWT token")
    void shouldGenerateTokenCorrectly() {
        User u = buildUser();
        u.setId(1L);
        Role r = buildRole();
        u.setRoles(List.of(r));
        LoginRequest request = new LoginRequest("lexa@mail.com", "12345678");

        when(userRepository.findByEmail("lexa@mail.com")).thenReturn(Optional.of(u));
        when(hashEncoder.matches(request.password(), u.getPassword())).thenReturn(true);
        when(jwtService.generateAccessToken(u.getId().toString(), u.getRoles()))
                .thenReturn("ajsudhfsiudef894ut534ngfi4w3gh8349gn");

        LoginResponse response = userService.login(request);

        assertEquals("ajsudhfsiudef894ut534ngfi4w3gh8349gn", response.token());

        verify(userRepository).findByEmail("lexa@mail.com");
        verify(hashEncoder).matches(request.password(), u.getPassword());
        verify(jwtService).generateAccessToken(u.getId().toString(), u.getRoles());
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("It should return the list of user roles.")
    void shouldReturnRoleList() {
        User u = buildUser();
        u.setId(1L);
        Role r = buildRole();
        u.setRoles(List.of(r));
        LoginRequest request = new LoginRequest("lexa@mail.com", "12345678");

        when(userRepository.findByEmail("lexa@mail.com")).thenReturn(Optional.of(u));
        when(hashEncoder.matches(request.password(), u.getPassword())).thenReturn(true);
        when(jwtService.generateAccessToken(u.getId().toString(), u.getRoles()))
                .thenReturn("ajsudhfsiudef894ut534ngfi4w3gh8349gn");

        LoginResponse response = userService.login(request);

        assertEquals(u.getRoles().stream().map(Role::getRole).toList(), response.roles());

        verify(userRepository).findByEmail("lexa@mail.com");
        verify(hashEncoder).matches(request.password(), u.getPassword());
        verify(jwtService).generateAccessToken(u.getId().toString(), u.getRoles());
    }

    @Test
    @DisplayName("It should return a LoginResponse with the correct information")
    void shouldReturnLoginResponseInfo() {
        User u = buildUser();
        u.setId(1L);
        Role r = buildRole();
        u.setRoles(List.of(r));
        LoginRequest request = new LoginRequest("lexa@mail.com", "12345678");

        when(userRepository.findByEmail("lexa@mail.com")).thenReturn(Optional.of(u));
        when(hashEncoder.matches(request.password(), u.getPassword())).thenReturn(true);
        when(jwtService.generateAccessToken(u.getId().toString(), u.getRoles()))
                .thenReturn("ajsudhfsiudef894ut534ngfi4w3gh8349gn");

        LoginResponse response = userService.login(request);

        assertEquals(u.getName(), response.name());
        assertEquals(u.getLastname(), response.lastname());
        assertEquals(u.getEmail(), response.email());

        verify(userRepository).findByEmail("lexa@mail.com");
        verify(hashEncoder).matches(request.password(), u.getPassword());
        verify(jwtService).generateAccessToken(u.getId().toString(), u.getRoles());
    }

    /*
     * Tests:
     * Debe devolver todos los usuarios existentes
     * Debe devolver una lista vacía cuando no existan usuarios
     */

    @Test
    @DisplayName("It should return all existing users.")
    void shouldReturnAllUsers() {
        User u = new User();
        u.setId(1L);
        User u2 = new User();
        u2.setId(3L);
        User u3 = new User();
        u3.setId(2L);

        List<User> usersList = new ArrayList<>();
        usersList.add(u);
        usersList.add(u2);
        usersList.add(u3);

        when(userRepository.findAll()).thenReturn(usersList);

        List<UserResponse> response = userService.getAllUsers();

        assertEquals(u.getName(), response.name());

        verify(userRepository).findAll();
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

}
