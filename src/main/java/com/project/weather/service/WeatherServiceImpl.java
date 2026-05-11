package com.project.weather.service;

import com.project.weather.client.OpenMeteoClient;
import com.project.weather.dto.WeatherResponse;
import com.project.weather.entity.City;
import com.project.weather.exception.CityNotFoundException;
import com.project.weather.repository.CityRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j                    // Provides 'log' object
@RequiredArgsConstructor   // Creates constructor for final fields
public class WeatherServiceImpl implements WeatherService {

    private final CityRepository cityRepository;
    private final OpenMeteoClient weatherClient;

    @Override
    @CircuitBreaker(name = "weather-api", fallbackMethod = "fallbackWeather")  // FIRST
    @Retry(name = "weather-api")  // SECOND
    @Cacheable(value = "weather", key = "#cityName", unless = "#result == null")  // LAST
    public WeatherResponse getCurrentWeather(String cityName) {
        log.info("CACHE MISS - Calling external API for: {}", cityName);

        City city = cityRepository.findByNameIgnoreCase(cityName)
                .orElseThrow(() -> new CityNotFoundException(cityName));

        WeatherResponse response = weatherClient.fetchCurrentWeather(city);
        log.info("Weather data fetched from API for: {}", cityName);

        return response;
    }

    // Fallback method - called when API fails or circuit is OPEN
    private WeatherResponse fallbackWeather(String cityName, Exception e) {
        log.warn("FALLBACK - Circuit OPEN or API failed for: {}", cityName);

        return WeatherResponse.builder()
                .city(cityName)
                .temperature(null)
                .temperatureUnit("Celsius")
                .weatherDescription("Weather service temporarily unavailable. Please try again later.")
                .source("fallback")
                .timestamp(LocalDateTime.now())
                .build();
    }
}