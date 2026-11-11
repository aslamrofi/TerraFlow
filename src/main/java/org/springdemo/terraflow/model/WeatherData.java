package org.springdemo.terraflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A single cached weather reading. Nothing fetches real data into this yet -
 * see WeatherServiceImpl, which seeds one row of mock data on first use.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherData {

    private String id;

    private String location;
    private Double temperature;
    private String condition;
    private Integer humidity;
    private Double windSpeed;
    private Double rainfall;

    private LocalDateTime recordedAt;
}
