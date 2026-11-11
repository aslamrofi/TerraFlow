package org.springdemo.terraflow.repository;

import org.springdemo.terraflow.model.WeatherData;

import java.util.Optional;

public interface WeatherRepository {
    WeatherData save(WeatherData data);
    Optional<WeatherData> findTopByOrderByRecordedAtDesc();
}
