package com.project.weather.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data                    // Getters, setters, toString
@Builder                 // Builder pattern: WeatherResponse.builder().city("Zagreb").build()
@NoArgsConstructor       // Default constructor
@AllArgsConstructor      // Constructor with all fields
public class WeatherResponse {
    private String city;
    private String country;
    private Double temperature;
    private String temperatureUnit;  // Celsius or Fahrenheit
    private Integer humidity;
    private Double windSpeed;
    private String windSpeedUnit;
    private String weatherDescription;
    private String source;  // "api", "cache", or "fallback"

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cachedAt;
}