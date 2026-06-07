package com.gymsocial.auth;

import com.auth0.jwt.JWT;
import com.gymsocial.user.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void createsAccessTokenForUser() {
        long userId = 42L;
        Instant now = Instant.now();
        User user = new User(
            userId,
            "Samuel",
            "samuel",
            "samuel@example.com",
            "hash",
            null,
            "ACTIVE",
            now,
            now
        );

        JwtService jwtService = new JwtService(
            "test-secret-with-at-least-32-characters"
        );
        String token = jwtService.createAccessToken(user);
        var decoded = JWT.decode(token);

        assertEquals("gym-social-api", decoded.getIssuer());
        assertEquals(String.valueOf(userId), decoded.getSubject());
        assertTrue(decoded.getExpiresAtAsInstant().isAfter(Instant.now()));
        assertEquals(userId, jwtService.verifyAccessToken(token));
    }

    @Test
    void rejectsTokenSignedWithDifferentSecret() {
        Instant now = Instant.now();
        User user = new User(
            42L,
            "Samuel",
            "samuel",
            "samuel@example.com",
            "hash",
            null,
            "ACTIVE",
            now,
            now
        );

        String token = new JwtService(
            "first-test-secret-with-at-least-32-characters"
        ).createAccessToken(user);
        JwtService jwtService = new JwtService(
            "second-test-secret-with-at-least-32-characters"
        );

        assertThrows(
            RuntimeException.class,
            () -> jwtService.verifyAccessToken(token)
        );
    }
}
