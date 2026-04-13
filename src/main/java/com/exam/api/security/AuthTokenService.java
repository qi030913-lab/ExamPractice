package com.exam.api.security;

import com.exam.model.User;
import com.exam.model.enums.UserRole;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthTokenService {
    private static final Duration TOKEN_TTL = Duration.ofHours(12);

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, AuthenticatedUser> sessions = new ConcurrentHashMap<>();

    public String issueToken(User user) {
        cleanupExpiredSessions();

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        UserRole role = user.getRole();
        LocalDateTime expiresAt = LocalDateTime.now().plus(TOKEN_TTL);
        sessions.put(token, new AuthenticatedUser(user.getUserId(), role, expiresAt));
        return token;
    }

    public AuthenticatedUser getAuthenticatedUser(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        String normalizedToken = token.trim();
        AuthenticatedUser authenticatedUser = sessions.get(normalizedToken);
        if (authenticatedUser == null) {
            return null;
        }

        if (authenticatedUser.getExpiresAt().isBefore(LocalDateTime.now())) {
            sessions.remove(normalizedToken);
            return null;
        }

        return authenticatedUser;
    }

    private void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        sessions.entrySet().removeIf(entry -> entry.getValue().getExpiresAt().isBefore(now));
    }
}
