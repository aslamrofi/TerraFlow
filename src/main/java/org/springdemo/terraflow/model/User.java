package org.springdemo.terraflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private String id;

    private String username;

    private String password; // stored as BCrypt hash

    private String fullName;

    private String email;

    private Role role;

    @Builder.Default
    private boolean enabled = true;

    private LocalDateTime createdAt;
}
