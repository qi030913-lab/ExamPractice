package com.exam.api.security;

import com.exam.model.User;
import com.exam.model.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthTokenService {
    private final Duration tokenTtl;
    private final int maxActiveSessions;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, SessionRecord> sessions = new ConcurrentHashMap<>();

    @Autowired
    public AuthTokenService(
            @Value("${exam.api.auth.token-ttl-hours:12}") long tokenTtlHours,
            @Value("${exam.api.auth.max-active-sessions:2048}") int maxActiveSessions
    ) {
        this(Duration.ofHours(Math.max(1, tokenTtlHours)), maxActiveSessions, Clock.systemDefaultZone());
    }

    public AuthTokenService() {
        this(Duration.ofHours(12), 2048, Clock.systemDefaultZone());
    }

    public AuthTokenService(Duration tokenTtl, int maxActiveSessions, Clock clock) {
        this.tokenTtl = tokenTtl == null || tokenTtl.isZero() || tokenTtl.isNegative() ? Duration.ofHours(12) : tokenTtl;
        this.maxActiveSessions = Math.max(1, maxActiveSessions);
        this.clock = clock == null ? Clock.systemDefaultZone() : clock;
    }

    public String issueToken(User user) {
        validateUser(user);
        cleanupExpiredSessions();
        evictOldestSessionIfNecessary();

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        LocalDateTime issuedAt = now();
        LocalDateTime expiresAt = issuedAt.plus(tokenTtl);
        sessions.put(token, new SessionRecord(user.getUserId(), user.getRole(), issuedAt, expiresAt));
        return token;
    }

    public AuthenticatedUser getAuthenticatedUser(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        String normalizedToken = token.trim();
        SessionRecord sessionRecord = sessions.get(normalizedToken);
        if (sessionRecord == null) {
            return null;
        }

        if (sessionRecord.isExpiredAt(now())) {
            sessions.remove(normalizedToken);
            return null;
        }

        return sessionRecord.toAuthenticatedUser();
    }

    @Scheduled(fixedDelayString = "${exam.api.auth.cleanup-interval-ms:300000}")
    public int cleanupExpiredSessions() {
        LocalDateTime now = now();
        int before = sessions.size();
        sessions.entrySet().removeIf(entry -> entry.getValue().isExpiredAt(now));
        return before - sessions.size();
    }

    public int getActiveSessionCount() {
        cleanupExpiredSessions();
        return sessions.size();
    }

    private void validateUser(User user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("用户信息不完整，无法签发令牌");
        }
        if (user.getRole() == null) {
            throw new IllegalArgumentException("用户角色不能为空");
        }
    }

    private void evictOldestSessionIfNecessary() {
        if (sessions.size() < maxActiveSessions) {
            return;
        }

        String oldestToken = sessions.entrySet().stream()
                .min(Comparator
                        .comparing((Map.Entry<String, SessionRecord> entry) -> entry.getValue().getIssuedAt())
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .orElse(null);
        if (oldestToken != null) {
            sessions.remove(oldestToken);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private static class SessionRecord {
        private final Integer userId;
        private final UserRole role;
        private final LocalDateTime issuedAt;
        private final LocalDateTime expiresAt;

        private SessionRecord(Integer userId, UserRole role, LocalDateTime issuedAt, LocalDateTime expiresAt) {
            this.userId = userId;
            this.role = role;
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
        }

        private LocalDateTime getIssuedAt() {
            return issuedAt;
        }

        private boolean isExpiredAt(LocalDateTime currentTime) {
            return expiresAt.isBefore(currentTime);
        }

        private AuthenticatedUser toAuthenticatedUser() {
            return new AuthenticatedUser(userId, role, expiresAt);
        }
    }
}
