package com.exam.tests.api.controller;

import com.exam.api.common.ApiResponse;
import com.exam.api.controller.HealthController;
import com.exam.api.dto.HealthResponse;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthControllerTest {

    @Test
    void healthShouldReturnTypedPayload() {
        HealthController controller = new HealthController();

        ApiResponse<HealthResponse> response = controller.health();

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("exam-api", response.getData().service());
        assertEquals("UP", response.getData().status());
        assertDoesNotThrow(() -> OffsetDateTime.parse(response.getData().timestamp()));
    }
}
