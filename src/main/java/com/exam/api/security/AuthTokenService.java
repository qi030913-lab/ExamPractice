package com.exam.api.security;

import com.exam.dao.AuthSessionDao;
import com.exam.model.AuthSession;
import com.exam.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class AuthTokenService {
    private final AuthSessionDao authSessionDao;
    private final Duration tokenTtl;
    private final int maxActiveSessions;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Object sessionMutationLock = new Object();

    @Autowired
    public AuthTokenService(
            AuthSessionDao authSessionDao,
            @Value("${exam.api.auth.token-ttl-hours:12}") long tokenTtlHours,
            @Value("${exam.api.auth.max-active-sessions:2048}") int maxActiveSessions
    ) {
        this(authSessionDao, Duration.ofHours(Math.max(1, tokenTtlHours)), maxActiveSessions, Clock.systemDefaultZone());
    }

    public AuthTokenService(AuthSessionDao authSessionDao, Duration tokenTtl, int maxActiveSessions, Clock clock) {
        this.authSessionDao = authSessionDao;
        this.tokenTtl = tokenTtl == null || tokenTtl.isZero() || tokenTtl.isNegative() ? Duration.ofHours(12) : tokenTtl;
        this.maxActiveSessions = Math.max(1, maxActiveSessions);
        this.clock = clock == null ? Clock.systemDefaultZone() : clock;
    }

    @PostConstruct
    public void initializeSessionStore() {
        authSessionDao.createTableIfMissing();
    }

    public String issueToken(User user) {
        validateUser(user);

        synchronized (sessionMutationLock) {
            cleanupExpiredSessionsInternal();
            evictOldestSessionIfNecessary();

            String token = generateToken();
            LocalDateTime issuedAt = now();

            AuthSession session = new AuthSession();
            session.setTokenHash(hashToken(token));
            session.setUserId(user.getUserId());
            session.setRole(user.getRole());
            session.setIssuedAt(issuedAt);
            session.setExpiresAt(issuedAt.plus(tokenTtl));
            authSessionDao.insert(session);
            return token;
        }
    }

    public AuthenticatedUser getAuthenticatedUser(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        AuthSession session = authSessionDao.findByTokenHash(hashToken(token.trim()));
        if (session == null) {
            return null;
        }

        if (!session.getExpiresAt().isAfter(now())) {
            authSessionDao.deleteByTokenHash(session.getTokenHash());
            return null;
        }

        return new AuthenticatedUser(session.getUserId(), session.getRole(), session.getExpiresAt());
    }

    public void invalidateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        authSessionDao.deleteByTokenHash(hashToken(token.trim()));
    }

    @Scheduled(fixedDelayString = "${exam.api.auth.cleanup-interval-ms:300000}")
    public int cleanupExpiredSessions() {
        synchronized (sessionMutationLock) {
            return cleanupExpiredSessionsInternal();
        }
    }

    public int getActiveSessionCount() {
        cleanupExpiredSessions();
        return authSessionDao.countSessions();
    }

    public Duration getTokenTtl() {
        return tokenTtl;
    }

    private void validateUser(User user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("用户信息不完整，无法签发会话");
        }
        if (user.getRole() == null) {
            throw new IllegalArgumentException("用户角色不能为空");
        }
    }

    private int cleanupExpiredSessionsInternal() {
        return authSessionDao.deleteExpiredSessions(now());
    }

    private void evictOldestSessionIfNecessary() {
        if (authSessionDao.countSessions() < maxActiveSessions) {
            return;
        }

        String oldestTokenHash = authSessionDao.findOldestTokenHash();
        if (oldestTokenHash != null && !oldestTokenHash.isEmpty()) {
            authSessionDao.deleteByTokenHash(oldestTokenHash);
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
