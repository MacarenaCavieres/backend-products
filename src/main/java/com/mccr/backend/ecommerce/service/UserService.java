package com.mccr.backend.ecommerce.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
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

import jakarta.transaction.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private HashEncoder hashEncoder;

    @Autowired
    private JwtService jwtService;

    @Transactional
    public UserResponse register(User user) {
        Optional<User> userFound = userRepository.findByEmail(user.getEmail().trim());

        if (userFound.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Error: Usuario ya registrado");
        }

        user.setPassword(hashEncoder.encode(user.getPassword()));
        Optional<Role> roleFound = roleRepository.findByRole(RoleList.ROLE_USER);
        if (!roleFound.isPresent()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error: El rol ROLE_USER no existe en la base de datos");
        }

        user.setRoles(List.of(roleFound.get()));

        User userCreated = userRepository.save(user);

        return new UserResponse(userCreated.getId(), userCreated.getName(), userCreated.getLastname(),
                userCreated.getEmail(), userCreated.getCreatedAt(), userCreated.getUpdatedAt());

    }

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        Optional<User> userFound = userRepository.findByEmail(loginRequest.getEmail().trim());

        if (!userFound.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Error: Usuario no registrado");
        }

        if (!hashEncoder.matches(loginRequest.getPassword(), userFound.get().getPassword())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "Error: Credenciales inválidas");
        }

        String token = jwtService.generateAccessToken(userFound.get().getId().toString(), userFound.get().getRoles());

        return new LoginResponse(userFound.get().getName(), userFound.get().getLastname(), userFound.get().getEmail(),
                token);

    }

    @Transactional
    public List<UserResponse> getAllUsers() {
        List<User> usersRaw = userRepository.findAll();

        return usersRaw.stream().map(u -> new UserResponse(u.getId(), u.getName(), u.getLastname(),
                u.getEmail(), u.getCreatedAt(), u.getUpdatedAt())).collect(Collectors.toList());

    }

    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
    @Transactional
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        return new UserResponse(user.getId(), user.getName(), user.getLastname(),
                user.getLastname(), user.getCreatedAt(), user.getUpdatedAt());
    }

    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
    @Transactional
    public UserResponse updateUser(Long id, User user) {
        User userById = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (!userById.getEmail().equals(user.getEmail())) {
            Optional<User> userByEmail = userRepository.findByEmail(user.getEmail());

            if (userByEmail.isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuario con ese email ya registrado");
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
                userUpdated.getEmail(), userUpdated.getCreatedAt(), userUpdated.getUpdatedAt());

    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void removeUser(Long id) {
        Optional<User> user = userRepository.findById(id);

        if (!user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        userRepository.deleteById(id);

    }

    @Transactional
    public UserResponse addSupervisorRole(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

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
                userUpdated.getEmail(), userUpdated.getCreatedAt(), userUpdated.getUpdatedAt());

    }

}
