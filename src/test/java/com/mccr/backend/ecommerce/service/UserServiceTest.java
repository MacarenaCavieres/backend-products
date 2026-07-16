package com.mccr.backend.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.mccr.backend.ecommerce.dto.ResetPasswordRequest;
import com.mccr.backend.ecommerce.dto.UserResponse;
import com.mccr.backend.ecommerce.model.Role;
import com.mccr.backend.ecommerce.model.User;
import com.mccr.backend.ecommerce.model.enums.RoleList;
import com.mccr.backend.ecommerce.repository.PasswordResetRepository;
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

    @Mock
    private PasswordResetRepository passwordResetRepository;

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

    @SuppressWarnings("null")
    @Test
    @DisplayName("It should return all existing users.")
    void shouldReturnAllUsers() {
        User u = buildUser();
        u.setId(1L);
        u.setRoles(List.of(buildRole()));
        User u2 = buildUser();
        u2.setId(3L);
        u2.setRoles(List.of(buildRole()));
        User u3 = buildUser();
        u3.setId(2L);
        u3.setRoles(List.of(buildRole()));

        List<User> usersList = new ArrayList<>();
        usersList.add(u);
        usersList.add(u2);
        usersList.add(u3);

        when(userRepository.findAll()).thenReturn(usersList);

        List<UserResponse> response = userService.getAllUsers();

        List<UserResponse> result = usersList.stream()
                .map(user -> new UserResponse(user.getId(), user.getName(), user.getLastname(), user.getEmail(),
                        user.getRoles().stream().map(Role::getRole).toList(), user.getCreatedAt(), user.getUpdatedAt()))
                .collect(Collectors.toList());

        assertEquals(result, response);
        assertEquals(result.size(), response.size());

        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("It should return an empty list when there are no users")
    void shouldReturnAnEmptyListOfUsers() {
        List<User> usersList = new ArrayList<>();

        when(userRepository.findAll()).thenReturn(usersList);

        List<UserResponse> users = userService.getAllUsers();

        assertEquals(usersList, users);

        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("It should return the user when it exists.")
    void shouldReturnExistingUserById() {
        User user = buildUser();
        user.setId(1L);
        user.setRoles(List.of(buildRole()));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertEquals("Alexandra", response.name());
        assertEquals("Trikru", response.lastname());
        assertEquals("lexa@mail.com", response.email());

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("It should throw a ResponseStatusException when the user does not exist")
    void shouldThrowExceptionWhenUserIsMissingInFindById() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.getUserById(1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Usuario no encontrado", ex.getReason());

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("It should throw a ResponseStatusException when the new email is already registered")
    void shouldThrowExceptionWhenUserEmailIsRegistered() {
        User user = buildUser();
        user.setId(1L);
        user.setRoles(List.of(buildRole()));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("lexa@email.com")).thenReturn(Optional.of(user));

        User newUserInfo = buildUser();
        newUserInfo.setId(1L);
        newUserInfo.setRoles(List.of(buildRole()));
        newUserInfo.setEmail("lexa@email.com");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.updateUser(1L, newUserInfo));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Usuario con ese email ya registrado", ex.getReason());

        verify(userRepository).findById(1L);
        verify(userRepository).findByEmail("lexa@email.com");
    }

    @Test
    @DisplayName("It should correctly update the first and last name")
    void shouldUpdateNameAndLastnameCorrectly() {
        User user = buildUser();
        user.setId(1L);
        user.setRoles(List.of(buildRole()));

        User newUserInfo = buildUser();
        newUserInfo.setId(1L);
        newUserInfo.setName("Lexa");
        newUserInfo.setLastname("Heda");
        newUserInfo.setRoles(List.of(buildRole()));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(hashEncoder.matches(user.getPassword(), newUserInfo.getPassword())).thenReturn(true);
        when(userRepository.save(user)).thenReturn(user);

        UserResponse result = userService.updateUser(1L, newUserInfo);

        assertEquals("Lexa", result.name());
        assertEquals("Heda", result.lastname());

        verify(userRepository).findById(1L);
        verify(hashEncoder).matches(user.getPassword(), newUserInfo.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("It should update the email address when the new email is not registered")
    void shouldUpdateEmailWhenTheNewIsNotRegistered() {
        User user = buildUser();
        user.setId(1L);
        user.setRoles(List.of(buildRole()));

        User newUserInfo = buildUser();
        newUserInfo.setEmail("lexa@email.com");
        newUserInfo.setRoles(List.of(buildRole()));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("lexa@email.com")).thenReturn(Optional.empty());
        when(hashEncoder.matches(user.getPassword(), newUserInfo.getPassword())).thenReturn(true);
        when(userRepository.save(user)).thenReturn(user);

        UserResponse result = userService.updateUser(1L, newUserInfo);

        assertEquals("lexa@email.com", result.email());

        verify(userRepository).findById(1L);
        verify(userRepository).findByEmail("lexa@email.com");
        verify(hashEncoder).matches(user.getPassword(), newUserInfo.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("It should not check if the email has changed when the email is the same")
    void shouldNotCheckWhenEmailIsTheSame() {
        User user = buildUser();
        user.setId(1L);
        user.setRoles(List.of(buildRole()));

        User newUserInfo = buildUser();
        newUserInfo.setRoles(List.of(buildRole()));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(hashEncoder.matches(user.getPassword(), newUserInfo.getPassword())).thenReturn(true);
        when(userRepository.save(user)).thenReturn(user);

        userService.updateUser(1L, newUserInfo);

        verify(userRepository).findById(1L);
        verify(userRepository, never()).findByEmail("lexa@mail.com");
        verify(hashEncoder).matches(user.getPassword(), newUserInfo.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("It should hash the new password when it changes")
    void shouldHashPassWhenItChanges() {
        User user = buildUser();
        user.setId(1L);
        user.setRoles(List.of(buildRole()));

        User newUserInfo = buildUser();
        newUserInfo.setPassword("123456789");
        newUserInfo.setRoles(List.of(buildRole()));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(hashEncoder.matches("123456789", user.getPassword())).thenReturn(false);
        when(hashEncoder.encode("123456789")).thenReturn("eiufhwes87y45h3n");
        when(userRepository.save(user)).thenReturn(user);

        userService.updateUser(1L, newUserInfo);

        assertNotEquals("123456789", user.getPassword());
        assertEquals("eiufhwes87y45h3n", user.getPassword());

        verify(userRepository).findById(1L);
        verify(hashEncoder).matches("123456789", "12345678");
        verify(hashEncoder).encode("123456789");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("It should not modified the pass when it is empty or null")
    void shouldNotModifiedAnEmptyOrNullPass() {
        User user = buildUser();
        user.setId(1L);
        user.setRoles(List.of(buildRole()));

        User newUserInfo = buildUser();
        newUserInfo.setPassword("");
        newUserInfo.setRoles(List.of(buildRole()));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.updateUser(1L, newUserInfo);

        verify(userRepository).findById(1L);
        verify(hashEncoder, never()).encode("");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("It should update the updatedAt date field")
    void shouldUpdatedUpdatedAtField() {
        User user = buildUser();
        user.setId(1L);
        user.setRoles(List.of(buildRole()));

        User newUserInfo = buildUser();
        newUserInfo.setName("Lexa");
        newUserInfo.setRoles(List.of(buildRole()));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(hashEncoder.matches("12345678", user.getPassword())).thenReturn(true);
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.updateUser(1L, newUserInfo);

        assertNotEquals(null, response.updatedAt());

        verify(userRepository).findById(1L);
        verify(hashEncoder).matches("12345678", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("It should save the user correctly")
    void shouldSaveTheUserCorrectlyAfterUpdateIt() {
        User user = buildUser();
        user.setId(1L);
        user.setRoles(List.of(buildRole()));

        User newUserInfo = buildUser();
        newUserInfo.setName("Lexa");
        newUserInfo.setRoles(List.of(buildRole()));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(hashEncoder.matches("12345678", user.getPassword())).thenReturn(true);
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.updateUser(1L, newUserInfo);

        assertEquals("Lexa", response.name());
        assertEquals("Trikru", response.lastname());
        assertEquals("lexa@mail.com", response.email());

        verify(userRepository).findById(1L);
        verify(hashEncoder).matches("12345678", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("It should delete the user if it exists")
    void shouldDeleteUserWhenExists() {
        User user = buildUser();
        user.setId(1L);
        user.setRoles(List.of(buildRole()));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.removeUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("It should throw a ResponseStatusException when the user does not exist in removeUser")
    void shouldThrowExceptionWhenUserDoesNotExistInRemoveUser() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.removeUser(1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Usuario no encontrado", ex.getReason());

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("It should throw a ResponseStatusException when user does not exist in assignRole")
    void shouldThrowExceptionWhenUserDoesNotExistInAssignSupervisorRole() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.addSupervisorRole(1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Usuario no encontrado", ex.getReason());

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("It should throw a ResponseStatusException when the ROLE_SUPERVISOR role does not exist")
    void shouldThrowExceptionWhenRoleSupervisorDoesNotExist() {
        User user = buildUser();
        user.setId(1L);
        user.setRoles(List.of(buildRole()));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByRole(RoleList.ROLE_SUPERVISOR)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.addSupervisorRole(1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("El rol ROLE_SUPERVISOR no está creado en la base de datos", ex.getReason());

        verify(userRepository).findById(1L);
        verify(roleRepository).findByRole(RoleList.ROLE_SUPERVISOR);
    }

    @Test
    @DisplayName("It should throw a ResponseStatusException when the user already has the role assigned")
    void shouldThrowExceptionWhenRoleSupervisorIsAlreadyAssignedToTheUser() {
        Role supervisorRole = new Role();
        supervisorRole.setId(2L);
        supervisorRole.setRole(RoleList.ROLE_SUPERVISOR);
        User user = buildUser();
        user.setId(1L);
        user.setRoles(List.of(buildRole(), supervisorRole));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByRole(RoleList.ROLE_SUPERVISOR)).thenReturn(Optional.of(supervisorRole));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.addSupervisorRole(1L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Usuario con ese rol ya asignado", ex.getReason());

        verify(userRepository).findById(1L);
        verify(roleRepository).findByRole(RoleList.ROLE_SUPERVISOR);
    }

    @Test
    @DisplayName("It should add the ROLE_SUPERVISOR role to the user")
    void shouldAddSupervisorRoleToTheUser() {
        Role supervisorRole = new Role();
        supervisorRole.setId(2L);
        supervisorRole.setRole(RoleList.ROLE_SUPERVISOR);

        User user = buildUser();
        user.setId(1L);

        List<Role> roles = new ArrayList<>();
        roles.add(buildRole());
        user.setRoles(roles);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByRole(RoleList.ROLE_SUPERVISOR)).thenReturn(Optional.of(supervisorRole));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.addSupervisorRole(1L);

        List<RoleList> rolesList = List.of(
                RoleList.ROLE_USER,
                RoleList.ROLE_SUPERVISOR);

        assertEquals(rolesList, response.roles());

        verify(userRepository).findById(1L);
        verify(roleRepository).findByRole(RoleList.ROLE_SUPERVISOR);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("It should update updatedAt")
    void shouldUpdatedUpdatedAtFieldInAssingSupervisorRole() {
        Role supervisorRole = new Role();
        supervisorRole.setId(2L);
        supervisorRole.setRole(RoleList.ROLE_SUPERVISOR);

        User user = buildUser();
        user.setId(1L);

        List<Role> roles = new ArrayList<>();
        roles.add(buildRole());
        user.setRoles(roles);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByRole(RoleList.ROLE_SUPERVISOR)).thenReturn(Optional.of(supervisorRole));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.addSupervisorRole(1L);

        assertNotEquals(null, response.updatedAt());

        verify(userRepository).findById(1L);
        verify(roleRepository).findByRole(RoleList.ROLE_SUPERVISOR);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("It should obtain the user ID from the token")
    void shouldObtainTheUserIdFromToken() {
        User u = buildUser();
        u.setId(1L);
        u.setRoles(List.of(buildRole()));

        when(jwtService.getUserIdFromToken("sdkjfgdsiufg234")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        UserResponse response = userService.getUserByToken("sdkjfgdsiufg234");

        assertEquals("Alexandra", response.name());
        assertEquals("Trikru", response.lastname());

        verify(jwtService).getUserIdFromToken("sdkjfgdsiufg234");
        verify(userRepository).findById(1L);

    }

    @Test
    @DisplayName("It should throw a ResponseStatusException when the user obtained from the token does not exist")
    void shouldThrowExceptionWhenUserDoesNotExistInGetUserByToken() {
        User u = buildUser();
        u.setId(1L);
        u.setRoles(List.of(buildRole()));

        when(jwtService.getUserIdFromToken("sdkjfgdsiufg234")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.getUserByToken("sdkjfgdsiufg234"));

        assertEquals("Usuario no encontrado", ex.getReason());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

        verify(jwtService).getUserIdFromToken("sdkjfgdsiufg234");
        verify(userRepository).findById(1L);

    }

    /*
     * Tests:
     * Debe eliminar los tokens anteriores del usuario.
     * Debe generar un nuevo token de recuperación.
     * Debe crear un PasswordResetToken con fecha de expiración.
     * Debe guardar el nuevo token en la base de datos.
     * Debe enviar el correo electrónico de recuperación.
     * Debe utilizar los roles del usuario para generar el token.
     */
    @Test
    @DisplayName("It should throw a ResponseStatusException when the user does not exist by email")
    void shouldThrowExceptionWhenUserDoesNotExistInRequestPasswordReset() {
        ResetPasswordRequest email = new ResetPasswordRequest("lexa@email.com");

        when(userRepository.findByEmail("lexa@email.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.requestPasswordReset(email));

        assertEquals("Usuario no encontrado", ex.getReason());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

        verify(userRepository).findByEmail("lexa@email.com");

    }

    // @Test
    // @DisplayName("It should delete the user's previous tokens")
    // void shouldDeleteUsersPreviousTokens1() {
    // User u = buildUser();
    // u.setId(1L);
    // u.setRoles(List.of(buildRole()));
    // ResetPasswordRequest email = new ResetPasswordRequest("lexa@email.com");

    // when(userRepository.findByEmail("lexa@email.com")).thenReturn(Optional.empty());
    // when(jwtService.generateResetPasswordToken("1",
    // u.getRoles())).thenReturn("siufh9w8yr237894bfnjesdnf");

    // ResponseStatusException ex = assertThrows(ResponseStatusException.class,
    // () -> userService.requestPasswordReset(email));

    // assertEquals("Usuario no encontrado", ex.getReason());
    // assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

    // verify(userRepository).findByEmail("lexa@email.com");

    // }

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
