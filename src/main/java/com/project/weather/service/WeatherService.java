package com.project.weather.service;

import com.project.weather.dto.WeatherResponse;

public interface WeatherService {
    WeatherResponse getCurrentWeather(String cityName);
}