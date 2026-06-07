package com.gymsocial.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    private final PasswordHasher passwordHasher = new PasswordHasher();

    @Test
    void hashesAndVerifiesPassword() {
        String password = "StrongPass1!";
        String hash = passwordHasher.hash(password);

        assertNotEquals(password, hash);
        assertTrue(passwordHasher.matches(password, hash));
        assertFalse(passwordHasher.matches("WrongPass1", hash));
    }
}
