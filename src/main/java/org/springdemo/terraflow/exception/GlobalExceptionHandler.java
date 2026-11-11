package org.springdemo.terraflow.exception;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        model.addAttribute("errorMessage", "You do not have permission to access this resource.");
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception ex, Model model) {
        model.addAttribute("errorMessage", "Something went wrong. Please try again.");
        return "error/500";
    }
}
