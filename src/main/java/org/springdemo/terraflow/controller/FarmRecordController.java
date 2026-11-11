package org.springdemo.terraflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdemo.terraflow.dto.FarmRecordDTO;
import org.springdemo.terraflow.service.FarmRecordService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/records")
@RequiredArgsConstructor
public class FarmRecordController {

    private final FarmRecordService farmRecordService;

    @GetMapping
    public String list(Model model, @AuthenticationPrincipal UserDetails principal) {
        model.addAttribute("records", farmRecordService.getAllForUser(principal.getUsername()));
        return "records/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("recordDTO", new FarmRecordDTO());
        return "records/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("recordDTO") FarmRecordDTO recordDTO,
                         BindingResult result,
                         @AuthenticationPrincipal UserDetails principal) {
        if (result.hasErrors()) {
            return "records/form";
        }
        farmRecordService.create(recordDTO, principal.getUsername());
        return "redirect:/records?created";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model,
                           @AuthenticationPrincipal UserDetails principal) {
        model.addAttribute("recordDTO", farmRecordService.getById(id, principal.getUsername()));
        return "records/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable String id,
                         @Valid @ModelAttribute("recordDTO") FarmRecordDTO recordDTO,
                         BindingResult result,
                         @AuthenticationPrincipal UserDetails principal) {
        if (result.hasErrors()) {
            return "records/form";
        }
        farmRecordService.update(id, recordDTO, principal.getUsername());
        return "redirect:/records?updated";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id, @AuthenticationPrincipal UserDetails principal) {
        farmRecordService.delete(id, principal.getUsername());
        return "redirect:/records?deleted";
    }
}