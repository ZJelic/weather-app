package com.project.weather.exception;

import com.project.weather.dto.ErrorResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Handle: City not found (from service layer)
    @ExceptionHandler(CityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCityNotFound(
            CityNotFoundException ex,
            HttpServletRequest request) {

        log.warn("City not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.getReasonPhrase())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Handle: Missing request parameter (e.g., no ?city=)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        log.warn("Missing required parameter: {}", ex.getParameterName());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Required parameter '" + ex.getParameterName() + "' is missing")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Handle: Validation errors (@NotBlank, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.warn("Validation error: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Invalid parameter: " + ex.getBindingResult().getAllErrors().get(0).getDefaultMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Handle: Any other unexpected exception (catch-all)
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleGenericError(
//            Exception ex,
//            HttpServletRequest request) {
//
//        log.error("Unexpected error: ", ex);
//
//        ErrorResponse error = ErrorResponse.builder()
//                .status(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
//                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
//                .message("Internal server error. Please try again later.")
//                .path(request.getRequestURI())
//                .timestamp(LocalDateTime.now())
//                .build();
//
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
//    }

    // Handle: Circuit breaker OPEN
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorResponse> handleCircuitBreakerOpen(
            CallNotPermittedException ex,
            HttpServletRequest request) {

        log.warn("Circuit breaker OPEN - returning service unavailable");

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .statusCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                .message("Weather service temporarily unavailable. Please try again later.")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handleRateLimiter(
            RequestNotPermitted ex,
            HttpServletRequest request) {

        log.warn("Rate limit exceeded for: {}", request.getRequestURI());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())
                .statusCode(HttpStatus.TOO_MANY_REQUESTS.value())
                .message("Too many requests. Please try again later.")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }
}