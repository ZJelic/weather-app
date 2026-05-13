package com.project.weather.service;

import com.project.weather.client.OpenMeteoClient;
import com.project.weather.dto.WeatherResponse;
import com.project.weather.entity.City;
import com.project.weather.exception.CityNotFoundException;
import com.project.weather.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UNIT TEST - Tests ONLY the service layer in isolation
 * Does NOT start Spring Boot, does NOT use real database or API
 * Uses Mocks (fake objects) to simulate dependencies
 */
@ExtendWith(MockitoExtension.class)  // Enables Mockito (@Mock, @InjectMocks)
class WeatherServiceUnitTest {

    // @Mock - Creates a fake version of this dependency
    // No real database is used
    @Mock
    private CityRepository cityRepository;

    // @Mock - Creates a fake version of the API client
    // No real external API is called
    @Mock
    private OpenMeteoClient weatherClient;

    // @InjectMocks - Creates the real service and injects the mocks above
    // This is what we are testing
    @InjectMocks
    private WeatherServiceImpl weatherService;

    private City testCity;

    // Runs BEFORE each test - sets up common test data
    @BeforeEach
    void setUp() {
        testCity = new City();
        testCity.setName("Zagreb");
        testCity.setCountry("HR");
        testCity.setLatitude(45.8150);
        testCity.setLongitude(15.9819);
    }

    // TEST CASE 1: Valid city - should return weather data
    @Test
    void getCurrentWeather_ValidCity_ReturnsWeather() {
        // ARRANGE - Set up what the mocks should return
        // When someone calls findByNameIgnoreCase with "Zagreb", return our test city
        when(cityRepository.findByNameIgnoreCase("Zagreb")).thenReturn(Optional.of(testCity));

        // Create a fake weather response that the API client should return
        WeatherResponse mockResponse = WeatherResponse.builder()
                .city("Zagreb")
                .country("HR")
                .temperature(15.5)
                .temperatureUnit("Celsius")
                .weatherDescription("Clear sky")
                .source("api")
                .build();

        // When someone calls fetchCurrentWeather with any City, return our fake response
        when(weatherClient.fetchCurrentWeather(any(City.class))).thenReturn(mockResponse);

        // ACT - Call the method we are testing
        WeatherResponse result = weatherService.getCurrentWeather("Zagreb");

        // ASSERT - Verify the result is what we expected
        assertNotNull(result);                                      // Result should not be null
        assertEquals("Zagreb", result.getCity());                   // City should be "Zagreb"
        assertEquals(15.5, result.getTemperature());                // Temperature should be 15.5
        assertEquals("Clear sky", result.getWeatherDescription());  // Description should match

        // VERIFY - Ensure the mocked methods were called the expected number of times
        verify(cityRepository).findByNameIgnoreCase("Zagreb");           // Called exactly once
        verify(weatherClient).fetchCurrentWeather(any(City.class));      // Called exactly once
    }

    // TEST CASE 2: Invalid city - should throw exception
    @Test
    void getCurrentWeather_InvalidCity_ThrowsCityNotFoundException() {
        // ARRANGE - When looking for "Atlantis", return empty (city not found)
        when(cityRepository.findByNameIgnoreCase("Atlantis")).thenReturn(Optional.empty());

        // ACT & ASSERT - Expect the method to throw an exception
        assertThrows(CityNotFoundException.class, () -> {
            weatherService.getCurrentWeather("Atlantis");
        });

        // VERIFY - Repository was called, but API client was NEVER called
        verify(cityRepository).findByNameIgnoreCase("Atlantis");
        verify(weatherClient, never()).fetchCurrentWeather(any(City.class));
    }
}