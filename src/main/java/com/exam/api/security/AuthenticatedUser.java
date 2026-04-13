package com.exam.api.security;

import com.exam.model.enums.UserRole;

import java.time.LocalDateTime;

public class AuthenticatedUser {
    private final Integer userId;
    private final UserRole role;
    private final LocalDateTime expiresAt;

    public AuthenticatedUser(Integer userId, UserRole role, LocalDateTime expiresAt) {
        this.userId = userId;
        this.role = role;
        this.expiresAt = expiresAt;
    }

    public Integer getUserId() {
        return userId;
    }

    public UserRole getRole() {
        return role;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
