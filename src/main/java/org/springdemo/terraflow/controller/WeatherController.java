package org.springdemo.terraflow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdemo.terraflow.service.WeatherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    /**
     * /weather                  -> page asks the browser for the device location,
     *                              then redirects to /weather?lat=..&lon=..
     * /weather?lat=..&lon=..    -> live weather for those coordinates
     */
    @GetMapping
    public String weather(@RequestParam(required = false) Double lat,
                          @RequestParam(required = false) Double lon,
                          @RequestParam(required = false) String place,
                          Model model) {
        if (lat != null && lon != null) {
            model.addAttribute("needsLocation", false);
            try {
                String cleanPlace = place == null ? null : place.strip();
                if (cleanPlace != null && (cleanPlace.isEmpty() || cleanPlace.length() > 100)) cleanPlace = null;
                model.addAttribute("weather", weatherService.getWeather(lat, lon, cleanPlace));
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", "Those coordinates are not valid.");
            } catch (Exception e) {
                log.error("Weather lookup failed", e);
                model.addAttribute("error", "Could not reach the weather service. Please try again in a moment.");
            }
        } else {
            model.addAttribute("needsLocation", true);
            model.addAttribute("weather", weatherService.getCurrentWeather()); // last saved reading, may be null
        }
        return "weather";
    }
}