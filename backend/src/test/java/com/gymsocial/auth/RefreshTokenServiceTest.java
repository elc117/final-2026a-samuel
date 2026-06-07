package com.gymsocial.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RefreshTokenServiceTest {

    private final RefreshTokenService refreshTokenService =
        new RefreshTokenService();

    @Test
    void generatesUniqueTokensAndHashesThem() {
        String firstToken = refreshTokenService.generateToken();
        String secondToken = refreshTokenService.generateToken();
        String tokenHash = refreshTokenService.hash(firstToken);

        assertNotEquals(firstToken, secondToken);
        assertNotEquals(firstToken, tokenHash);
        assertEquals(64, tokenHash.length());
        assertEquals(tokenHash, refreshTokenService.hash(firstToken));
    }
}
