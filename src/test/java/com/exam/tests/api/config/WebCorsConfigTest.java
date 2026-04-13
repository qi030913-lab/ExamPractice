package com.exam.tests.api.config;

import com.exam.api.config.WebCorsConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class WebCorsConfigTest {
    @Test
    void parseAllowedOriginPatternsShouldTrimAndDeduplicate() {
        String[] result = WebCorsConfig.parseAllowedOriginPatterns(" null, http://127.0.0.1:5173 ,null,http://localhost:5173 ");

        assertArrayEquals(new String[]{
                "null",
                "http://127.0.0.1:5173",
                "http://localhost:5173"
        }, result);
    }

    @Test
    void parseAllowedOriginPatternsShouldFallbackWhenBlank() {
        String[] result = WebCorsConfig.parseAllowedOriginPatterns("  ,   ");

        assertArrayEquals(new String[]{
                "null",
                "http://127.0.0.1:5173",
                "http://localhost:5173"
        }, result);
    }
}
