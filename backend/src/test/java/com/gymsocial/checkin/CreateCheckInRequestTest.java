package com.gymsocial.checkin;

import com.gymsocial.checkin.dto.CreateCheckInRequest;
import com.gymsocial.shared.exception.ValidationException;
import com.gymsocial.shared.validation.RequestValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateCheckInRequestTest {

    private final RequestValidator validator = new RequestValidator();

    @Test
    void acceptsCheckInWithoutDescription() {
        var request = new CreateCheckInRequest("Treino concluído", null);

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void rejectsInvalidTitleAndLongDescription() {
        var request = new CreateCheckInRequest(
            "a",
            "a".repeat(1001)
        );

        var exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request)
        );

        assertEquals(2, exception.errors().size());
    }
}
