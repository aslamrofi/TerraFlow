package org.springdemo.terraflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdemo.terraflow.dto.CropDTO;
import org.springdemo.terraflow.service.CropService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/crops")
@RequiredArgsConstructor
public class CropController {

    private final CropService cropService;

    // READ (list) - T: view registered crops
    @GetMapping
    public String list(Model model, @AuthenticationPrincipal UserDetails principal) {
        model.addAttribute("crops", cropService.getAllForUser(principal.getUsername()));
        return "crop/list";
    }

    // READ (single)
    @GetMapping("/{id}")
    public String details(@PathVariable String id, Model model,
                          @AuthenticationPrincipal UserDetails principal) {
        model.addAttribute("crop", cropService.getById(id, principal.getUsername()));
        return "crop/details";
    }

    // CREATE - show form - T2: register a new crop
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("cropDTO", new CropDTO());
        return "crop/form";
    }

    // CREATE - submit
    @PostMapping
    public String create(@Valid @ModelAttribute("cropDTO") CropDTO cropDTO,
                         BindingResult result,
                         @AuthenticationPrincipal UserDetails principal,
                         Model model) {
        if (result.hasErrors()) {
            return "crop/form";
        }
        cropService.create(cropDTO, principal.getUsername());
        return "redirect:/crops?created";
    }

    // UPDATE - show form
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model,
                           @AuthenticationPrincipal UserDetails principal) {
        model.addAttribute("cropDTO", cropService.getById(id, principal.getUsername()));
        return "crop/form";
    }

    // UPDATE - submit
    @PostMapping("/{id}")
    public String update(@PathVariable String id,
                         @Valid @ModelAttribute("cropDTO") CropDTO cropDTO,
                         BindingResult result,
                         @AuthenticationPrincipal UserDetails principal) {
        if (result.hasErrors()) {
            return "crop/form";
        }
        cropService.update(id, cropDTO, principal.getUsername());
        return "redirect:/crops/" + id + "?updated";
    }

    // DELETE
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id, @AuthenticationPrincipal UserDetails principal) {
        cropService.delete(id, principal.getUsername());
        return "redirect:/crops?deleted";
    }
}
