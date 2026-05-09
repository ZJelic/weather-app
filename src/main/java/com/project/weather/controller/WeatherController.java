package com.project.weather.controller;

import com.project.weather.dto.WeatherResponse;
import com.project.weather.service.WeatherService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/weather")
@Validated
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/current")
    public WeatherResponse getCurrentWeather(
            @RequestParam(required = true)
            @NotBlank(message = "City name is required")
            String city) {
        return weatherService.getCurrentWeather(city);
    }
}