package com.mccr.backend.ecommerce.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mccr.backend.ecommerce.config.HashEncoder;
import com.mccr.backend.ecommerce.dto.LoginRequest;
import com.mccr.backend.ecommerce.dto.LoginResponse;
import com.mccr.backend.ecommerce.dto.RegisterResponse;
import com.mccr.backend.ecommerce.model.Role;
import com.mccr.backend.ecommerce.model.User;
import com.mccr.backend.ecommerce.model.enums.RoleList;
import com.mccr.backend.ecommerce.repository.RoleRepository;
import com.mccr.backend.ecommerce.repository.UserRepository;

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

    public RegisterResponse register(User user) {
        Optional<User> userFound = userRepository.findByEmail(user.getEmail().trim());

        if (userFound.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Error: Usuario ya registrado");
        }

        user.setPassword(hashEncoder.encode(user.getPassword()));
        List<Role> roles = roleRepository.findByRole(RoleList.ROLE_USER);
        user.setRoles(roles);

        User userCreated = userRepository.save(user);

        return new RegisterResponse(userCreated.getId(), userCreated.getName(), userCreated.getLastname(),
                userCreated.getEmail(), userCreated.getCreatedAt(), userCreated.getUpdatedAt());

    }

    public LoginResponse login(LoginRequest loginRequest) {
        Optional<User> userFound = userRepository.findByEmail(loginRequest.getEmail().trim());

        if (!userFound.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Error: Usuario no registrado");
        }

        boolean itMatches = hashEncoder.matches(loginRequest.getPassword(), userFound.get().getPassword());

        if (!itMatches) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "Error: Credenciales inválidas");
        }

        String token = jwtService.generateAccessToken(userFound.get().getId().toString(), userFound.get().getRoles());

        return new LoginResponse(userFound.get().getName(), userFound.get().getLastname(), userFound.get().getEmail(),
                token);

    }

}
