package org.springdemo.terraflow.service;

import org.springdemo.terraflow.dto.WeatherDTO;

import java.util.List;

public interface WeatherService {

    /** Live weather + 5 day forecast. 'place' is the name found by the browser; may be null (then looked up server-side). */
    WeatherDTO getWeather(double latitude, double longitude, String place);

    /** Last reading saved in Firebase, or null if there is none yet. */
    WeatherDTO getCurrentWeather();

    /** Kept so existing callers still compile; the forecast now lives in WeatherDTO.getForecast(). */
    List<WeatherDTO.ForecastDay> getForecast();
}