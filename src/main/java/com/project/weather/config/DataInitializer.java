package com.project.weather.config;

import com.project.weather.entity.City;
import com.project.weather.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CityRepository cityRepository;

    @Override
    public void run(String... args) {
        log.info("Loading cities into H2 database...");

        City[] cities = {
                new City(null, "Zagreb", 45.8150, 15.9819, "HR"),
                new City(null, "London", 51.5074, -0.1278, "GB"),
                new City(null, "New York", 40.7128, -74.0060, "US"),
                new City(null, "Tokyo", 35.6762, 139.6503, "JP"),
                new City(null, "Paris", 48.8566, 2.3522, "FR")
        };

        for (City city : cities) {
            cityRepository.save(city);
        }

        log.info("Loaded {} cities", cityRepository.count());
    }
}