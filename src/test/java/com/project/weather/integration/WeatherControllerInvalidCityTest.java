package com.project.weather.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"api.key=test-api-key-for-unit-tests"}
)
@ActiveProfiles("test")
class WeatherControllerInvalidCityTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${api.key:test-api-key-for-unit-tests}")
    private String validApiKey;

    @BeforeEach
    void resetCircuitBreaker() {
        try {
            // Try to reset circuit breaker via actuator endpoint
            restTemplate.postForEntity("/actuator/circuitbreakers/weather-api/reset", null, String.class);
            System.out.println("Circuit breaker reset via actuator");
        } catch (Exception e) {
            // If actuator endpoint not available, make a successful request to close the circuit
            System.out.println("Actuator reset not available, using fallback method");
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("api", validApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            try {
                // Make a request to a valid city to close the circuit
                restTemplate.exchange(
                        "/api/v1/weather/current?city=Zagreb",
                        HttpMethod.GET,
                        entity,
                        String.class
                );
                System.out.println("Circuit breaker closed by successful request");
            } catch (Exception ex) {
                System.out.println("Could not close circuit breaker: " + ex.getMessage());
            }
        }
    }

    @Test
    void getCurrentWeather_InvalidCity_ReturnsNotFound() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("api", validApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/weather/current?city=Atlantis",
                HttpMethod.GET,
                entity,
                String.class
        );

        // PRINT THE ACTUAL RESPONSE TO SEE WHAT'S WRONG
        System.out.println("=== ACTUAL RESPONSE ===");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
        System.out.println("======================");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}