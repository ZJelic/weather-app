package com.project.weather.client;

import com.project.weather.dto.WeatherResponse;
import com.project.weather.entity.City;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OpenMeteoClient {

    private final WebClient webClient;
    private final Duration timeout;

    public OpenMeteoClient(@Value("${weather.api.base-url}") String baseUrl,
                           @Value("${weather.api.timeout:3s}") Duration timeout) {
        this.timeout = timeout;

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) timeout.toMillis())
                .responseTimeout(timeout)
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(timeout.getSeconds(), TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(timeout.getSeconds(), TimeUnit.SECONDS))
                );

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(httpClient))
                .build();
    }

    public WeatherResponse fetchCurrentWeather(City city) {
        log.info("Calling external weather API for: {}", city.getName());

        // NO try-catch - let exceptions propagate to Resilience4j
        OpenMeteoResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast")
                        .queryParam("latitude", city.getLatitude())
                        .queryParam("longitude", city.getLongitude())
                        .queryParam("current_weather", true)
                        .queryParam("hourly", "temperature_2m,relativehumidity_2m,windspeed_10m")
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse ->
                        Mono.error(new RuntimeException("API returned 4xx error: " + clientResponse.statusCode())))
                .onStatus(status -> status.is5xxServerError(), clientResponse ->
                        Mono.error(new RuntimeException("API returned 5xx error: " + clientResponse.statusCode())))
                .bodyToMono(OpenMeteoResponse.class)
                .block(timeout);

        if (response == null) {
            throw new RuntimeException("No response from weather API within timeout");
        }

        Integer currentHumidity = extractCurrentHumidity(response);

        return WeatherResponse.builder()
                .city(city.getName())
                .country(city.getCountry())
                .temperature(response.currentWeather.temperature)
                .temperatureUnit("Celsius")
                .humidity(currentHumidity)
                .windSpeed(response.currentWeather.windspeed)
                .windSpeedUnit("km/h")
                .weatherDescription(getWeatherDescription(response.currentWeather.weathercode))
                .source("api")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private Integer extractCurrentHumidity(OpenMeteoResponse response) {
        if (response.hourly != null && response.hourly.relativehumidity_2m != null
                && !response.hourly.relativehumidity_2m.isEmpty()) {
            return response.hourly.relativehumidity_2m.get(0);
        }
        return null;
    }

    private String getWeatherDescription(Integer code) {
        if (code == null) return "Unknown";
        return switch (code) {
            case 0 -> "Clear sky";
            case 1, 2, 3 -> "Partly cloudy";
            case 45, 48 -> "Foggy";
            case 51, 53, 55 -> "Drizzle";
            case 61, 63, 65 -> "Rain";
            case 71, 73, 75 -> "Snow";
            case 95 -> "Thunderstorm";
            default -> "Weather data available";
        };
    }

    // JSON mapping classes
    public static class OpenMeteoResponse {
        @JsonProperty("current_weather")
        public CurrentWeather currentWeather;
        public Hourly hourly;
    }

    public static class CurrentWeather {
        public Double temperature;
        public Double windspeed;
        public Integer weathercode;
    }

    public static class Hourly {
        public java.util.List<String> time;
        @JsonProperty("relativehumidity_2m")
        public java.util.List<Integer> relativehumidity_2m;
    }
}