package com.auction.server.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hashPasswordShouldCreateDifferentHashAndVerifySuccessfully() {
        String rawPassword = "P@ssw0rd123";

        String hashed = PasswordUtil.hashPassword(rawPassword);

        assertNotNull(hashed);
        assertNotEquals(rawPassword, hashed);
        assertTrue(PasswordUtil.checkPassword(rawPassword, hashed));
    }

    @Test
    void checkPasswordShouldFailForWrongPassword() {
        String hashed = PasswordUtil.hashPassword("correct-password");

        assertFalse(PasswordUtil.checkPassword("wrong-password", hashed));
    }
}

