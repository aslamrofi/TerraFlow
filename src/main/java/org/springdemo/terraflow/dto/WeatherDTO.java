package org.springdemo.terraflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherDTO {

    private String location;
    private Double latitude;
    private Double longitude;

    private Double temperature;
    private String condition;
    /** Bootstrap Icons class, e.g. "bi-cloud-rain". Null for old saved readings. */
    private String icon;
    private Integer humidity;
    private Double windSpeed;
    private Double rainfall;

    private List<ForecastDay> forecast;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ForecastDay {
        private LocalDate date;
        private String condition;
        private String icon;
        private Double minTemp;
        private Double maxTemp;
    }
}