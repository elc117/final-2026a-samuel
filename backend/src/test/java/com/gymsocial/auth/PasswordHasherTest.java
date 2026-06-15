package com.gymsocial.auth;

import com.password4j.Argon2Function;
import com.password4j.Password;
import com.password4j.types.Argon2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void usesMemoryCostSuitableForLimitedContainers() {
        String hash = passwordHasher.hash("StrongPass1!");
        Argon2Function function = Argon2Function.getInstanceFromHash(hash);

        assertEquals(19_456, function.getMemory());
        assertEquals(2, function.getIterations());
        assertEquals(1, function.getParallelism());
        assertEquals(Argon2.ID, function.getVariant());
    }

    @Test
    void verifiesHashesCreatedWithPreviousParameters() {
        String password = "StrongPass1!";
        Argon2Function previousFunction = Argon2Function.getInstance(
            12_288,
            3,
            1,
            32,
            Argon2.ID
        );
        String previousHash = Password.hash(password)
            .with(previousFunction)
            .getResult();

        assertTrue(passwordHasher.matches(password, previousHash));
    }
}
