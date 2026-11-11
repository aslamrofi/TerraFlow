package org.springdemo.terraflow.controller;

import lombok.RequiredArgsConstructor;
import org.springdemo.terraflow.service.CropService;
import org.springdemo.terraflow.service.FarmRecordService;
import org.springdemo.terraflow.service.FarmTaskService;
import org.springdemo.terraflow.service.WeatherService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final CropService cropService;
    private final FarmRecordService farmRecordService;
    private final FarmTaskService farmTaskService;
    private final WeatherService weatherService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails principal) {
        String username = principal.getUsername();
        var crops = cropService.getAllForUser(username);

        model.addAttribute("cropCount", crops.size());
        model.addAttribute("recentCrops", crops.stream().limit(5).toList());
        model.addAttribute("recordCount", farmRecordService.getAllForUser(username).size());
        model.addAttribute("pendingTaskCount", farmTaskService.countActiveForUser(username));
        model.addAttribute("upcomingTasks", farmTaskService.getUpcomingForUser(username, 5));
        model.addAttribute("weather", weatherService.getCurrentWeather());
        return "dashboard";
    }
}
