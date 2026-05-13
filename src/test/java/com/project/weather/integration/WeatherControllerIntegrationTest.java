package com.project.weather.integration;

import com.project.weather.dto.WeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * INTEGRATION TEST - Tests the FULL application with real components
 * Starts Spring Boot, uses real database (H2), calls real external API
 * Uses "test" profile to override configuration
 *
 * FIXED: Reset circuit breaker before each test
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"api.key=test-api-key-for-unit-tests"}  // ← FORCE the property
)
@ActiveProfiles("test")  // Use application-test.yml for configuration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)  // ← Tests run in @Order sequence
class WeatherControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;  // Spring's tool for testing REST calls

    @Value("${api.key:test-api-key-for-unit-tests}")  // Read API key from test profile (with fallback)
    private String validApiKey;

    // Reset circuit breaker before EACH test
    @BeforeEach
    void resetCircuitBreaker() {
        try {
            // Try to reset circuit breaker via actuator (if endpoint is available)
            restTemplate.postForEntity("/actuator/circuitbreakers/weather-api/reset", null, String.class);
        } catch (Exception e) {
            // If endpoint not available, just log and continue
            System.out.println("Could not reset circuit breaker via actuator: " + e.getMessage());
        }
    }

    // TEST CASE 1: Non-existent city - should return 404
    // ORDER 1: Runs FIRST - circuit breaker is reset before this test
    @Test
    @Order(1)  // ← Runs first
    void getCurrentWeather_InvalidCity_ReturnsNotFound() {
        // ARRANGE - Valid headers, but invalid city
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("api", validApiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // ACT
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/weather/current?city=Atlantis",  // ← City that doesn't exist
                HttpMethod.GET,
                entity,
                String.class
        );

        // ASSERT - Should return 404 Not Found (circuit is closed initially)
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // TEST CASE 2: Valid request with correct API key
    // ORDER 2: Runs second - after InvalidCity test
    @Test
    @Order(2)  // ← Runs second
    void getCurrentWeather_ValidRequest_ReturnsWeatherData() {
        // ARRANGE - Create headers with valid API key (Basic Auth)
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("api", validApiKey);  // Username: api, Password: validApiKey

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // ACT - Make the actual HTTP request
        ResponseEntity<WeatherResponse> response = restTemplate.exchange(
                "/api/v1/weather/current?city=Zagreb",  // URL
                HttpMethod.GET,                         // HTTP method
                entity,                                 // Headers (with API key)
                WeatherResponse.class                   // Expected response type
        );

        // ASSERT - Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());     // Status 200 OK
        assertNotNull(response.getBody());                         // Body not null
        assertEquals("Zagreb", response.getBody().getCity());       // Correct city
        assertNotNull(response.getBody().getTemperature());         // Temperature exists
    }

    // TEST CASE 3: No API key - should be unauthorized
    @Test
    @Order(3)  // ← Runs third
    void getCurrentWeather_NoApiKey_ReturnsUnauthorized() {
        // ARRANGE - No headers, no authentication
        HttpEntity<String> entity = new HttpEntity<>(null);

        // ACT
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/weather/current?city=Zagreb",
                HttpMethod.GET,
                entity,
                String.class
        );

        // ASSERT - Should return 401 Unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // TEST CASE 4: Wrong API key - should be unauthorized
    @Test
    @Order(4)  // ← Runs fourth
    void getCurrentWeather_WrongApiKey_ReturnsUnauthorized() {
        // ARRANGE - Create headers with WRONG password
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("api", "wrong-key");  // ← Incorrect password

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // ACT
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/weather/current?city=Zagreb",
                HttpMethod.GET,
                entity,
                String.class
        );

        // ASSERT - Should return 401 Unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}