package com.gymsocial.auth;

import com.gymsocial.auth.dto.LoginRequest;
import com.gymsocial.auth.dto.RegisterRequest;
import com.gymsocial.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthValidatorTest {

    @Test
    void acceptsValidRegistration() {
        var request = new RegisterRequest(
            "Samuel Test",
            "samuel.test",
            "samuel@example.com",
            "StrongPass1"
        );

        assertDoesNotThrow(() -> AuthValidator.validate(request));
    }

    @Test
    void rejectsInvalidRegistrationFields() {
        var request = new RegisterRequest(
            "",
            "invalid user",
            "invalid-email",
            "weak"
        );

        var exception = assertThrows(
            ValidationException.class,
            () -> AuthValidator.validate(request)
        );

        assertEquals(4, exception.errors().size());
    }

    @Test
    void rejectsLoginWithoutPassword() {
        var request = new LoginRequest("samuel@example.com", "");

        var exception = assertThrows(
            ValidationException.class,
            () -> AuthValidator.validate(request)
        );

        assertEquals("Informe sua senha.", exception.errors().get("password"));
    }
}
