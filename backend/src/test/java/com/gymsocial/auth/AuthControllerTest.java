package com.gymsocial.auth;

import io.javalin.http.SameSite;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AuthControllerTest {

    @Test
    void createsSecureRefreshCookieWithoutInvalidDomain() {
        var controller = new AuthController(null, true, "Lax");

        var cookie = controller.createRefreshTokenCookie("token", 3600);

        assertEquals("refresh_token", cookie.getName());
        assertEquals("/", cookie.getPath());
        assertEquals(3600, cookie.getMaxAge());
        assertTrue(cookie.getSecure());
        assertTrue(cookie.isHttpOnly());
        assertNull(cookie.getDomain());
        assertEquals(SameSite.LAX, cookie.getSameSite());
    }
}
