# Weather API

Spring Boot application that fetches current weather data from Open-Meteo API with caching, circuit breaker, rate limiting, and API key security.

# Tech Stack

- Java 21
- Spring Boot 3.4.4
- Spring Security
- Spring Data JPA
- Spring WebClient
- Resilience4j
- Redis
- H2 Database
- Docker
- Maven

# Features

- REST endpoint: GET /api/v1/weather/current?city=Zagreb
- H2 database with preloaded cities (Zagreb, London, Paris, Tokyo, New York)
- Redis caching (prod profile)
- Caffeine caching (dev profile)
- Circuit breaker + retry + fallback
- Rate limiting: 5 requests per 60 seconds
- API key authentication with Basic Auth
- Redis password protection
- Actuator health and metrics
- Swagger UI
- Correlation ID for request tracing
- Docker and Docker Compose support
- Unit and integration tests

# Environment Setup

Create .env file in project root:

REDIS_PASSWORD=your-redis-password
API_KEY=your-api-key

IntelliJ users:
Install EnvFile plugin to load .env automatically.

# Run Application

Start Redis:

docker run -d --name redis -p 6379:6379 redis:7-alpine

Run with prod profile:
Uses Redis cache.

Run with dev profile:
Uses in-memory Caffeine cache (no Redis required).

# API Usage

## Authentication

All endpoints require Basic Auth.

- Username: api
- Password: your API_KEY from .env

## Example Request

- Method: GET
- URL: http://localhost:8080/api/v1/weather/current?city=Zagreb
- Auth: Basic Auth (Username: api, Password: your API_KEY)

## Example Response

{
  "city": "Zagreb",
  "country": "HR",
  "temperature": 15.5,
  "temperatureUnit": "Celsius",
  "humidity": 72,
  "windSpeed": 5.2,
  "weatherDescription": "Clear sky",
  "source": "api",
  "timestamp": "2026-05-13 15:30:00"
}

## Rate Limiting

- 5 requests per 60 seconds
- 6th request returns HTTP 429 Too Many Requests

## Health Check

GET http://localhost:8080/actuator/health

## Swagger UI

http://localhost:8080/swagger-ui.html

## H2 Console

http://localhost:8080/h2-console

### H2 Configuration

- JDBC URL: jdbc:h2:mem:weatherdb
- Username: sa
- Password: empty
