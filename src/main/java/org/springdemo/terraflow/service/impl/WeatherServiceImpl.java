package org.springdemo.terraflow.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdemo.terraflow.dto.WeatherDTO;
import org.springdemo.terraflow.model.WeatherData;
import org.springdemo.terraflow.repository.WeatherRepository;
import org.springdemo.terraflow.service.WeatherService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Live weather from Open-Meteo (free, no API key) for coordinates sent by the
 * browser. The place name comes from OpenStreetMap Nominatim. Each reading is
 * also saved to Firebase (at most once every 30 minutes) as a history/fallback.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast";
    private static final String GEOCODE_URL = "https://nominatim.openstreetmap.org/reverse";
    // Nominatim requires an identifying User-Agent. Put your real contact email here.
    private static final String USER_AGENT = "TerraFlow/0.0.1 (your-email@example.com)";
    private static final Duration SAVE_EVERY = Duration.ofMinutes(30);
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private final WeatherRepository weatherRepository;
    private final RestClient http = buildClient();

    private static RestClient buildClient() {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
        factory.setReadTimeout(Duration.ofSeconds(12));
        return RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public WeatherDTO getWeather(double lat, double lon, String place) {
        if (Double.isNaN(lat) || Double.isNaN(lon) || lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            throw new IllegalArgumentException("Invalid coordinates");
        }
        String latS = String.format(Locale.ROOT, "%.4f", lat);
        String lonS = String.format(Locale.ROOT, "%.4f", lon);

        Map<String, Object> body = fetchWithRetry(latS, lonS);

        if (body == null || !(body.get("current") instanceof Map<?, ?> current)) {
            throw new IllegalStateException("Empty response from weather provider");
        }

        List<WeatherDTO.ForecastDay> forecast = new ArrayList<>();
        if (body.get("daily") instanceof Map<?, ?> daily
                && daily.get("time") instanceof List<?> times
                && daily.get("weather_code") instanceof List<?> codes
                && daily.get("temperature_2m_max") instanceof List<?> maxes
                && daily.get("temperature_2m_min") instanceof List<?> mins) {
            int n = Math.min(Math.min(times.size(), codes.size()), Math.min(maxes.size(), mins.size()));
            for (int i = 1; i < n; i++) { // index 0 is today, shown as "current"
                Integer code = toInt(codes.get(i));
                forecast.add(WeatherDTO.ForecastDay.builder()
                        .date(LocalDate.parse(String.valueOf(times.get(i))))
                        .condition(describe(code))
                        .icon(icon(code))
                        .minTemp(toDouble(mins.get(i)))
                        .maxTemp(toDouble(maxes.get(i)))
                        .build());
            }
        }

        Integer code = toInt(current.get("weather_code"));
        String name = (place != null && !place.isBlank()) ? place : lookupPlaceName(latS, lonS);

        WeatherDTO dto = WeatherDTO.builder()
                .location(name != null ? name : latS + ", " + lonS)
                .latitude(lat)
                .longitude(lon)
                .temperature(toDouble(current.get("temperature_2m")))
                .condition(describe(code))
                .icon(icon(code))
                .humidity(toInt(current.get("relative_humidity_2m")))
                .windSpeed(toDouble(current.get("wind_speed_10m")))
                .rainfall(toDouble(current.get("precipitation")))
                .forecast(forecast)
                .build();

        saveSnapshot(dto);
        return dto;
    }

    @Override
    public WeatherDTO getCurrentWeather() {
        try {
            return weatherRepository.findTopByOrderByRecordedAtDesc().map(this::toDto).orElse(null);
        } catch (Exception e) {
            log.warn("Could not read saved weather: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<WeatherDTO.ForecastDay> getForecast() {
        return List.of();
    }

    // ---- helpers -------------------------------------------------------

    private Map<String, Object> fetchWithRetry(String latS, String lonS) {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return http.get()
                        .uri(FORECAST_URL + "?latitude={lat}&longitude={lon}"
                                        + "&current=temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m"
                                        + "&daily=weather_code,temperature_2m_max,temperature_2m_min"
                                        + "&timezone=auto&forecast_days=6",
                                latS, lonS)
                        .retrieve()
                        .body(MAP_TYPE);
            } catch (Exception e) {
                last = (e instanceof RuntimeException re) ? re : new RuntimeException(e);
                log.warn("Weather fetch attempt {} failed: {}", attempt, e.getMessage());
                try { Thread.sleep(500L * attempt); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
        throw last;
    }

    private void saveSnapshot(WeatherDTO dto) {
        try {
            Optional<WeatherData> last = weatherRepository.findTopByOrderByRecordedAtDesc();
            boolean stale = last.map(WeatherData::getRecordedAt)
                    .map(t -> t.isBefore(LocalDateTime.now().minus(SAVE_EVERY)))
                    .orElse(true);
            if (stale) {
                weatherRepository.save(WeatherData.builder()
                        .location(dto.getLocation())
                        .temperature(dto.getTemperature())
                        .condition(dto.getCondition())
                        .humidity(dto.getHumidity())
                        .windSpeed(dto.getWindSpeed())
                        .rainfall(dto.getRainfall())
                        .recordedAt(LocalDateTime.now())
                        .build());
            }
        } catch (Exception e) {
            log.warn("Could not save weather reading: {}", e.getMessage());
        }
    }

    private WeatherDTO toDto(WeatherData d) {
        return WeatherDTO.builder()
                .location(d.getLocation())
                .temperature(d.getTemperature())
                .condition(d.getCondition())
                .humidity(d.getHumidity())
                .windSpeed(d.getWindSpeed())
                .rainfall(d.getRainfall())
                .forecast(List.of())
                .build();
    }

    private String lookupPlaceName(String lat, String lon) {
        try {
            Map<String, Object> geo = http.get()
                    .uri(GEOCODE_URL + "?format=jsonv2&zoom=14&addressdetails=1&accept-language=en&lat={lat}&lon={lon}",
                            lat, lon)
                    .header("User-Agent", USER_AGENT)
                    .retrieve()
                    .body(MAP_TYPE);
            if (geo != null && geo.get("address") instanceof Map<?, ?> addr) {
                String district = firstNonBlank(addr, "suburb", "city_district", "neighbourhood", "quarter");
                String city = firstNonBlank(addr, "city", "town", "village", "municipality", "county");
                if (district != null && city != null && !district.equalsIgnoreCase(city)) return district + ", " + city;
                if (city != null) return city;
                if (district != null) return district;
                return firstNonBlank(addr, "state", "country");
            }
        } catch (Exception e) {
            log.warn("Place name lookup failed: {}", e.getMessage());
        }
        return null;
    }

    private static String firstNonBlank(Map<?, ?> map, String... keys) {
        for (String k : keys) {
            if (map.get(k) instanceof String s && !s.isBlank()) return s;
        }
        return null;
    }

    private static Double toDouble(Object o) {
        return o instanceof Number n ? n.doubleValue() : null;
    }

    private static Integer toInt(Object o) {
        return o instanceof Number n ? Integer.valueOf((int) Math.round(n.doubleValue())) : null;
    }

    /** WMO weather codes as used by Open-Meteo. */
    private static String describe(Integer code) {
        if (code == null) return "Unknown";
        return switch (code) {
            case 0 -> "Clear sky";
            case 1 -> "Mainly clear";
            case 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Fog";
            case 51, 53, 55 -> "Drizzle";
            case 56, 57 -> "Freezing drizzle";
            case 61 -> "Light rain";
            case 63 -> "Rain";
            case 65 -> "Heavy rain";
            case 66, 67 -> "Freezing rain";
            case 71, 73, 75, 77 -> "Snow";
            case 80, 81 -> "Rain showers";
            case 82 -> "Violent rain showers";
            case 85, 86 -> "Snow showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with hail";
            default -> "Unknown";
        };
    }

    private static String icon(Integer code) {
        if (code == null) return "bi-cloud";
        return switch (code) {
            case 0, 1 -> "bi-sun";
            case 2 -> "bi-cloud-sun";
            case 3 -> "bi-clouds";
            case 45, 48 -> "bi-cloud-fog2";
            case 51, 53, 55, 56, 57 -> "bi-cloud-drizzle";
            case 61, 63, 66, 67, 80, 81 -> "bi-cloud-rain";
            case 65, 82 -> "bi-cloud-rain-heavy";
            case 71, 73, 75, 77, 85, 86 -> "bi-cloud-snow";
            case 95, 96, 99 -> "bi-cloud-lightning-rain";
            default -> "bi-cloud";
        };
    }
}