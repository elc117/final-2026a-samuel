package com.gymsocial.shared.pagination;

import com.gymsocial.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class InstantUuidCursorCodecTest {

    private final InstantUuidCursorCodec codec =
        new InstantUuidCursorCodec();

    @Test
    void shouldEncodeAndDecodeCursor() {
        Instant createdAt = Instant.parse("2026-06-10T12:30:00Z");
        UUID id = UUID.fromString(
            "6d15732e-92aa-4cf3-afc1-6042d6efe4ce"
        );

        var decoded = codec.decode(codec.encode(createdAt, id));

        assertEquals(createdAt, decoded.createdAt());
        assertEquals(id, decoded.id());
    }

    @Test
    void shouldRejectInvalidCursor() {
        assertThrows(
            ValidationException.class,
            () -> codec.decode("invalid-cursor")
        );
    }
}
