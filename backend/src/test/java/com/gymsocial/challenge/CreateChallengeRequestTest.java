package com.gymsocial.challenge;

import com.gymsocial.challenge.dto.CreateChallengeRequest;
import com.gymsocial.shared.exception.ValidationException;
import com.gymsocial.shared.validation.RequestValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateChallengeRequestTest {

    private final RequestValidator validator = new RequestValidator();

    @Test
    void acceptsValidChallenge() {
        assertDoesNotThrow(() -> validator.validate(
            new CreateChallengeRequest(
                "Treinar toda semana",
                null,
                "WEEKLY",
                null,
                false
            )
        ));
    }

    @Test
    void rejectsMissingRequiredFields() {
        assertThrows(
            ValidationException.class,
            () -> validator.validate(new CreateChallengeRequest(
                " ",
                null,
                " ",
                null,
                false
            ))
        );
    }
}
