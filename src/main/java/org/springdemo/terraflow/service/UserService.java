package org.springdemo.terraflow.service;

import org.springdemo.terraflow.dto.UserDTO;
import org.springdemo.terraflow.model.User;

public interface UserService {
    User register(UserDTO userDTO);
    boolean usernameExists(String username);
    boolean emailExists(String email);
}
