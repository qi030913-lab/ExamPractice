package com.exam.api.config;

import com.exam.api.security.AuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebCorsConfig implements WebMvcConfigurer {
    private static final String[] DEFAULT_ALLOWED_ORIGIN_PATTERNS = {
            "null",
            "http://127.0.0.1:5173",
            "http://localhost:5173"
    };

    private final AuthInterceptor authInterceptor;
    private final String[] allowedOriginPatterns;

    public WebCorsConfig(
            AuthInterceptor authInterceptor,
            @Value("${exam.api.cors.allowed-origin-patterns:null,http://127.0.0.1:5173,http://localhost:5173}")
            String allowedOriginPatternsValue
    ) {
        this.authInterceptor = authInterceptor;
        this.allowedOriginPatterns = parseAllowedOriginPatterns(allowedOriginPatternsValue);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOriginPatterns)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/health");
    }

    public static String[] parseAllowedOriginPatterns(String rawValue) {
        String[] configuredPatterns = Arrays.stream(rawValue.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .toArray(String[]::new);
        return configuredPatterns.length == 0 ? DEFAULT_ALLOWED_ORIGIN_PATTERNS.clone() : configuredPatterns;
    }
}
