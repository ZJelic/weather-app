package com.project.weather.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {
    private String status;      // "Not Found", "Bad Request", etc.
    private int statusCode;     // 404, 400, 500
    private String message;     // Human-readable error
    private String path;        // Which endpoint caused it
    private String correlationId;  // To trace request in logs

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}