package org.springdemo.terraflow.controller;

import lombok.RequiredArgsConstructor;
import org.springdemo.terraflow.model.UserPreference;
import org.springdemo.terraflow.repository.UserPreferenceRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final UserPreferenceRepository userPreferenceRepository;

    @GetMapping
    public String settings(Model model, @AuthenticationPrincipal UserDetails principal) {
        model.addAttribute("preference", findOrDefault(principal.getUsername()));
        return "settings";
    }

    @PostMapping
    public String save(@ModelAttribute("preference") UserPreference submitted,
                       @AuthenticationPrincipal UserDetails principal) {
        UserPreference preference = userPreferenceRepository.findByOwnerUsername(principal.getUsername())
                .orElseGet(() -> UserPreference.builder().ownerUsername(principal.getUsername()).build());
        preference.setLanguage(submitted.getLanguage());
        preference.setTemperatureUnit(submitted.getTemperatureUnit());
        preference.setEmailNotifications(submitted.isEmailNotifications());
        userPreferenceRepository.save(preference);
        return "redirect:/settings?saved";
    }

    private UserPreference findOrDefault(String username) {
        return userPreferenceRepository.findByOwnerUsername(username)
                .orElseGet(() -> UserPreference.builder()
                        .ownerUsername(username)
                        .language("en")
                        .temperatureUnit("C")
                        .emailNotifications(true)
                        .build());
    }
}
