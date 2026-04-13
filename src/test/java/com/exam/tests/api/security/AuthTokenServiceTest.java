package com.exam.tests.api.security;

import com.exam.api.security.AuthTokenService;
import com.exam.model.User;
import com.exam.model.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthTokenServiceTest {
    @Test
    void tokenShouldExpireAfterConfiguredTtl() {
        MutableClock clock = new MutableClock("2026-04-13T10:00:00Z");
        AuthTokenService authTokenService = new AuthTokenService(Duration.ofMinutes(5), 10, clock);

        String token = authTokenService.issueToken(buildUser(7, UserRole.STUDENT));
        assertNotNull(authTokenService.getAuthenticatedUser(token));

        clock.advance(Duration.ofMinutes(6));

        assertNull(authTokenService.getAuthenticatedUser(token));
    }

    @Test
    void tokenShouldExpireExactlyAtExpiryBoundary() {
        MutableClock clock = new MutableClock("2026-04-13T10:00:00Z");
        AuthTokenService authTokenService = new AuthTokenService(Duration.ofMinutes(5), 10, clock);

        String token = authTokenService.issueToken(buildUser(8, UserRole.STUDENT));
        clock.advance(Duration.ofMinutes(5));

        assertNull(authTokenService.getAuthenticatedUser(token));
        assertEquals(0, authTokenService.getActiveSessionCount());
    }

    @Test
    void issueTokenShouldEvictOldestSessionWhenCapacityReached() {
        MutableClock clock = new MutableClock("2026-04-13T10:00:00Z");
        AuthTokenService authTokenService = new AuthTokenService(Duration.ofHours(12), 2, clock);

        String firstToken = authTokenService.issueToken(buildUser(1, UserRole.STUDENT));
        clock.advance(Duration.ofSeconds(1));
        String secondToken = authTokenService.issueToken(buildUser(2, UserRole.STUDENT));
        clock.advance(Duration.ofSeconds(1));
        String thirdToken = authTokenService.issueToken(buildUser(3, UserRole.TEACHER));

        assertNull(authTokenService.getAuthenticatedUser(firstToken));
        assertNotNull(authTokenService.getAuthenticatedUser(secondToken));
        assertNotNull(authTokenService.getAuthenticatedUser(thirdToken));
        assertEquals(2, authTokenService.getActiveSessionCount());
    }

    @Test
    void issueTokenShouldNotExceedCapacityUnderConcurrentRequests() throws Exception {
        AuthTokenService authTokenService = new AuthTokenService(Duration.ofHours(12), 3, Clock.systemUTC());
        ExecutorService executor = Executors.newFixedThreadPool(6);
        CountDownLatch ready = new CountDownLatch(6);
        CountDownLatch start = new CountDownLatch(1);

        try {
            @SuppressWarnings("unchecked")
            Future<String>[] futures = new Future[6];
            for (int i = 0; i < 6; i++) {
                final int userId = i + 1;
                futures[i] = executor.submit((Callable<String>) () -> {
                    ready.countDown();
                    assertTrue(start.await(5, TimeUnit.SECONDS));
                    return authTokenService.issueToken(buildUser(userId, UserRole.STUDENT));
                });
            }

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            for (Future<String> future : futures) {
                String token = future.get(5, TimeUnit.SECONDS);
                assertNotNull(token);
            }

            assertEquals(3, authTokenService.getActiveSessionCount());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void cleanupExpiredSessionsShouldRemoveExpiredEntries() {
        MutableClock clock = new MutableClock("2026-04-13T10:00:00Z");
        AuthTokenService authTokenService = new AuthTokenService(Duration.ofMinutes(1), 10, clock);

        authTokenService.issueToken(buildUser(11, UserRole.STUDENT));
        clock.advance(Duration.ofMinutes(2));

        assertEquals(1, authTokenService.cleanupExpiredSessions());
        assertEquals(0, authTokenService.getActiveSessionCount());
    }

    @Test
    void issueTokenShouldRejectUserWithoutId() {
        AuthTokenService authTokenService = new AuthTokenService();
        User user = new User();
        user.setRole(UserRole.STUDENT);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authTokenService.issueToken(user));

        assertEquals("用户信息不完整，无法签发令牌", exception.getMessage());
    }

    private User buildUser(int userId, UserRole role) {
        User user = new User();
        user.setUserId(userId);
        user.setRole(role);
        return user;
    }

    private static class MutableClock extends Clock {
        private Instant currentInstant;
        private final ZoneId zoneId = ZoneId.systemDefault();

        private MutableClock(String instantText) {
            this.currentInstant = Instant.parse(instantText);
        }

        private void advance(Duration duration) {
            currentInstant = currentInstant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zoneId;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return currentInstant;
        }
    }
}
