package com.project.weather.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/cache")
public class CacheController {

    @Autowired
    private CacheManager cacheManager;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        cacheManager.getCacheNames().stream().forEach(cacheName -> {
            Map<String, Object> cacheStats = new HashMap<>();
            cacheStats.put("name", cacheName);
            cacheStats.put("type", cacheManager.getClass().getSimpleName());
            stats.put(cacheName, cacheStats);
        });

        stats.put("availableCaches", cacheManager.getCacheNames());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/clear")
    public ResponseEntity<String> clearAllCaches() {
        cacheManager.getCacheNames().stream()
                .forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
        return ResponseEntity.ok("All caches cleared");
    }
}