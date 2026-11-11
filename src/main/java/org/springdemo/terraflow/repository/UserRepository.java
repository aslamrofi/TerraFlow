package org.springdemo.terraflow.repository;

import org.springdemo.terraflow.model.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(String id);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
