package org.springdemo.terraflow.security;

import lombok.RequiredArgsConstructor;
import org.springdemo.terraflow.dto.UserDTO;
import org.springdemo.terraflow.service.UserService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Logs a person in by username alone. If the username is new, an account is
 * created on the spot (with a random, never-shown password).
 */
@Component
@RequiredArgsConstructor
public class UsernameOnlyAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName() == null ? "" : authentication.getName().trim();
        if (username.isEmpty()) {
            throw new BadCredentialsException("Username is required");
        }

        UserDetails user;
        try {
            user = userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException ex) {
            userService.register(UserDTO.builder()
                    .username(username)
                    .fullName(username)
                    .email(username.toLowerCase().replaceAll("[^a-z0-9._-]", "") + "@terraflow.local")
                    .password(UUID.randomUUID().toString())
                    .build());
            user = userDetailsService.loadUserByUsername(username);
        }
        return UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
