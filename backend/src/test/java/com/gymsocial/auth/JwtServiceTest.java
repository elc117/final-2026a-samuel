package com.gymsocial.auth;

import com.auth0.jwt.JWT;
import com.gymsocial.user.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void createsAccessTokenForUser() {
        UUID userId = UUID.randomUUID();
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

        String token = new JwtService(
            "test-secret-with-at-least-32-characters"
        ).createAccessToken(user);
        var decoded = JWT.decode(token);

        assertEquals("gym-social-api", decoded.getIssuer());
        assertEquals(userId.toString(), decoded.getSubject());
        assertTrue(decoded.getExpiresAtAsInstant().isAfter(Instant.now()));
    }
}
