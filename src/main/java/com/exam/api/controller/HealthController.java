package com.exam.api.controller;

import com.exam.api.common.ApiResponse;
import com.exam.api.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<HealthResponse> health() {
        return ApiResponse.success(
                "服务已就绪",
                new HealthResponse("exam-api", "UP", OffsetDateTime.now().toString())
        );
    }
}
