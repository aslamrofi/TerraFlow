package org.springdemo.terraflow.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Makes the logged-in username available to every template as ${currentUsername}. */
@ControllerAdvice
public class CurrentUserAdvice {

    @ModelAttribute("currentUsername")
    public String currentUsername(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication.getName();
    }
}