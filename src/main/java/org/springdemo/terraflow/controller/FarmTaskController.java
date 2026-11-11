package org.springdemo.terraflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdemo.terraflow.dto.FarmTaskDTO;
import org.springdemo.terraflow.service.FarmTaskService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class FarmTaskController {

    private final FarmTaskService farmTaskService;

    @GetMapping
    public String list(Model model, @AuthenticationPrincipal UserDetails principal) {
        model.addAttribute("tasks", farmTaskService.getAllForUser(principal.getUsername()));
        return "task/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("taskDTO", new FarmTaskDTO());
        return "task/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("taskDTO") FarmTaskDTO taskDTO,
                         BindingResult result,
                         @AuthenticationPrincipal UserDetails principal) {
        if (result.hasErrors()) {
            return "task/form";
        }
        farmTaskService.create(taskDTO, principal.getUsername());
        return "redirect:/tasks?created";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model,
                           @AuthenticationPrincipal UserDetails principal) {
        model.addAttribute("taskDTO", farmTaskService.getById(id, principal.getUsername()));
        return "task/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable String id,
                         @Valid @ModelAttribute("taskDTO") FarmTaskDTO taskDTO,
                         BindingResult result,
                         @AuthenticationPrincipal UserDetails principal) {
        if (result.hasErrors()) {
            return "task/form";
        }
        farmTaskService.update(id, taskDTO, principal.getUsername());
        return "redirect:/tasks?updated";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id, @AuthenticationPrincipal UserDetails principal) {
        farmTaskService.delete(id, principal.getUsername());
        return "redirect:/tasks?deleted";
    }
}
