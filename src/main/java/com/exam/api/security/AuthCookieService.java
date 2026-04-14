package com.exam.api.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieService {
    private final String cookieName;
    private final boolean secureCookie;
    private final String sameSite;

    public AuthCookieService(
            @Value("${exam.api.auth.cookie-name:EXAM_SESSION}") String cookieName,
            @Value("${exam.api.auth.cookie-secure:true}") boolean secureCookie,
            @Value("${exam.api.auth.cookie-same-site:None}") String sameSite
    ) {
        this.cookieName = cookieName == null || cookieName.trim().isEmpty() ? "EXAM_SESSION" : cookieName.trim();
        this.secureCookie = secureCookie;
        this.sameSite = sameSite == null || sameSite.trim().isEmpty() ? "None" : sameSite.trim();
    }

    public AuthCookieService() {
        this("EXAM_SESSION", true, "None");
    }

    public String resolveToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String bearerToken = extractBearerToken(request.getHeader("Authorization"));
        if (bearerToken != null) {
            return bearerToken;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie != null && cookieName.equals(cookie.getName())) {
                String value = cookie.getValue();
                if (value != null && !value.trim().isEmpty()) {
                    return value.trim();
                }
            }
        }

        return null;
    }

    public void writeLoginCookie(HttpServletResponse response, String token, Duration ttl) {
        if (response == null || token == null || token.trim().isEmpty()) {
            return;
        }

        Duration maxAge = ttl == null || ttl.isNegative() ? Duration.ZERO : ttl;
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(cookieName, token.trim())
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/")
                .maxAge(maxAge)
                .build()
                .toString());
    }

    public void clearAuthCookie(HttpServletResponse response) {
        if (response == null) {
            return;
        }

        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ZERO)
                .build()
                .toString());
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null) {
            return null;
        }

        String prefix = "Bearer ";
        if (!authorization.startsWith(prefix)) {
            return null;
        }

        String token = authorization.substring(prefix.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
