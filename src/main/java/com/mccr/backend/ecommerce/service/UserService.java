package com.mccr.backend.ecommerce.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.mccr.backend.ecommerce.dto.UserDto;
import com.mccr.backend.ecommerce.model.User;
import com.mccr.backend.ecommerce.repository.UserRepository;

public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserDto register(User user) {
        List<User> userFound = userRepository.findByEmail(user.getEmail().trim());

        if (!userFound.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Error: Usuario ya registrado");
        }

        User userCreated = userRepository.save(user);

        return new UserDto(userCreated.getId(), userCreated.getName(), userCreated.getLastname(),
                userCreated.getEmail(), userCreated.getCreatedAt(), userCreated.getUpdatedAt());

    }

}
