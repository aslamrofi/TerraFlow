package org.springdemo.terraflow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springdemo.terraflow.dto.UserDTO;
import org.springdemo.terraflow.model.Role;
import org.springdemo.terraflow.model.User;
import org.springdemo.terraflow.repository.UserRepository;
import org.springdemo.terraflow.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(UserDTO dto) {
        if (usernameExists(dto.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + dto.getUsername());
        }
        if (emailExists(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .role(Role.FARMER) // default role for self-registration
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    @Override
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
