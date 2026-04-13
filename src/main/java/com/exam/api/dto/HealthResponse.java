package com.exam.api.dto;

public record HealthResponse(
        String service,
        String status,
        String timestamp
) {
}
