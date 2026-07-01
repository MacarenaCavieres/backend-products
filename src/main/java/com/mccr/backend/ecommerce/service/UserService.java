package com.mccr.backend.ecommerce.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mccr.backend.ecommerce.config.HashEncoder;
import com.mccr.backend.ecommerce.dto.LoginRequest;
import com.mccr.backend.ecommerce.dto.LoginResponse;
import com.mccr.backend.ecommerce.dto.RecoveryPassword;
import com.mccr.backend.ecommerce.dto.ResetPasswordRequest;
import com.mccr.backend.ecommerce.dto.UserResponse;
import com.mccr.backend.ecommerce.model.PasswordResetToken;
import com.mccr.backend.ecommerce.model.Role;
import com.mccr.backend.ecommerce.model.User;
import com.mccr.backend.ecommerce.model.enums.RoleList;
import com.mccr.backend.ecommerce.repository.PasswordResetRepository;
import com.mccr.backend.ecommerce.repository.RoleRepository;
import com.mccr.backend.ecommerce.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final HashEncoder hashEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @SuppressWarnings("null")
    @Transactional
    public UserResponse register(User user) {
        userRepository.findByEmail(user.getEmail().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Error: Usuario ya registrado"));

        user.setPassword(hashEncoder.encode(user.getPassword()));
        Role roleFound = roleRepository.findByRole(RoleList.ROLE_USER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error: El rol ROLE_USER no existe en la base de datos"));

        user.setRoles(List.of(roleFound));

        User userCreated = userRepository.save(user);

        return new UserResponse(userCreated.getId(), userCreated.getName(), userCreated.getLastname(),
                userCreated.getEmail(), userCreated.getRoles().stream().map(Role::getRole).toList(),
                userCreated.getCreatedAt(), userCreated.getUpdatedAt());

    }

    @SuppressWarnings("null")
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        User userFound = userRepository.findByEmail(loginRequest.email().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Error: Usuario no registrado"));

        if (!hashEncoder.matches(loginRequest.password(), userFound.getPassword())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "Error: Credenciales inválidas");
        }

        String token = jwtService.generateAccessToken(userFound.getId().toString(), userFound.getRoles());
        List<Role> roles = userFound.getRoles();
        List<RoleList> roleNames = roles.stream().map(Role::getRole).toList();

        return new LoginResponse(userFound.getName(), userFound.getLastname(), userFound.getEmail(),
                token, roleNames);

    }

    @SuppressWarnings("null")
    @Transactional
    public List<UserResponse> getAllUsers() {
        List<User> usersRaw = userRepository.findAll();

        return usersRaw.stream().map(u -> new UserResponse(u.getId(), u.getName(), u.getLastname(),
                u.getEmail(), u.getRoles().stream().map(Role::getRole).toList(), u.getCreatedAt(),
                u.getUpdatedAt()))
                .collect(Collectors.toList());

    }

    @SuppressWarnings("null")
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
    @Transactional
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));

        return new UserResponse(user.getId(), user.getName(), user.getLastname(),
                user.getLastname(), user.getRoles().stream().map(Role::getRole).toList(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    @SuppressWarnings("null")
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
    @Transactional
    public UserResponse updateUser(Long id, User user) {
        User userById = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));

        if (!userById.getEmail().equals(user.getEmail())) {
            Optional<User> userByEmail = userRepository.findByEmail(user.getEmail());

            if (userByEmail.isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Usuario con ese email ya registrado");
            }
            userById.setEmail(user.getEmail());

        }

        userById.setName(user.getName());
        userById.setLastname(user.getLastname());

        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()
                && !hashEncoder.matches(user.getPassword(), userById.getPassword())) {
            userById.setPassword(hashEncoder.encode(user.getPassword()));
        }

        userById.setUpdatedAt(Instant.now());

        User userUpdated = userRepository.save(userById);

        return new UserResponse(userUpdated.getId(), userUpdated.getName(), userUpdated.getLastname(),
                userUpdated.getEmail(), userUpdated.getRoles().stream().map(Role::getRole).toList(),
                userUpdated.getCreatedAt(), userUpdated.getUpdatedAt());

    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void removeUser(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));

        userRepository.deleteById(id);

    }

    @SuppressWarnings("null")
    @Transactional
    public UserResponse addSupervisorRole(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));

        Role supervisorRole = roleRepository.findByRole(RoleList.ROLE_SUPERVISOR)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El rol ROLE_SUPERVISOR no está creado en la base de datos"));

        if (user.getRoles().contains(supervisorRole)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuario con ese rol ya asignado");
        }

        user.getRoles().add(supervisorRole);
        user.setUpdatedAt(Instant.now());

        User userUpdated = userRepository.save(user);

        return new UserResponse(userUpdated.getId(), userUpdated.getName(), userUpdated.getLastname(),
                userUpdated.getEmail(), userUpdated.getRoles().stream().map(Role::getRole).toList(),
                userUpdated.getCreatedAt(), userUpdated.getUpdatedAt());

    }

    @SuppressWarnings("null")
    @Transactional
    public UserResponse getUserByToken(String token) {
        String id = jwtService.getUserIdFromToken(token);
        User user = userRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));

        return new UserResponse(user.getId(), user.getName(), user.getLastname(),
                user.getEmail(), user.getRoles().stream().map(Role::getRole).toList(),
                user.getCreatedAt(),
                user.getUpdatedAt());

    }

    @Transactional
    public void requestPasswordReset(ResetPasswordRequest resetPasswordRequest) {
        User user = userRepository.findByEmail(resetPasswordRequest.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));

        passwordResetRepository.deleteByUserId(user.getId());

        List<Role> rolesList = user.getRoles();

        String newResetPasswordToken = jwtService.generateResetPasswordToken(resetPasswordRequest.email(),
                rolesList);
        Long expiration = jwtService.expirationTokenResetTime;
        Instant expiresAt = Instant.now().plusMillis(expiration);

        PasswordResetToken resetToken = new PasswordResetToken(newResetPasswordToken, user.getId(), expiresAt);

        passwordResetRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), newResetPasswordToken);

    }

    @Transactional
    public String recoveryPassword(RecoveryPassword recoveryInfo) {

        boolean isValidToken = jwtService.validateAcessToken(recoveryInfo.token());

        if (!isValidToken) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expirado, solicite otro");
        }

        Optional<PasswordResetToken> optionalToken = passwordResetRepository.findByToken(recoveryInfo.token());

        if (!optionalToken.isPresent() || optionalToken.get().isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token ya usado, solicite otro");
        }

        User user = userRepository.findById(optionalToken.get().getUserId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario asociado a ese token no encontrado"));

        user.setPassword(hashEncoder.encode(recoveryInfo.password()));
        userRepository.save(user);

        optionalToken.get().setUsed(true);
        passwordResetRepository.save(optionalToken.get());

        return "Contraseña restablecida";

    }

}
