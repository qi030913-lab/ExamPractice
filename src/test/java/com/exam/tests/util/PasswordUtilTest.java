package com.exam.tests.util;

import com.exam.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hashAndMatchShouldWork() {
        String hash = PasswordUtil.hashPassword("abc12345");
        assertTrue(hash.startsWith("PBKDF2$"));
        assertTrue(PasswordUtil.matches("abc12345", hash));
        assertFalse(PasswordUtil.matches("wrong", hash));
    }

    @Test
    void plainPasswordShouldStillMatchForMigration() {
        assertTrue(PasswordUtil.matches("123456", "123456"));
        assertTrue(PasswordUtil.needsMigration("123456"));
    }

    @Test
    void hashedPasswordShouldNotNeedMigration() {
        String hash = PasswordUtil.hashPassword("abc12345");
        assertFalse(PasswordUtil.needsMigration(hash));
    }
}
