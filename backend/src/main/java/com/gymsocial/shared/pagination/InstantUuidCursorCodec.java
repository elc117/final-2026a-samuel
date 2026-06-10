package com.gymsocial.shared.pagination;

import com.gymsocial.shared.exception.ValidationException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public final class InstantUuidCursorCodec {

    public String encode(Instant createdAt, UUID id) {
        String value = "%s|%s".formatted(createdAt, id);
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public InstantUuidCursor decode(String encodedCursor) {
        if (encodedCursor == null || encodedCursor.isBlank()) {
            return null;
        }

        try {
            String value = new String(
                Base64.getUrlDecoder().decode(encodedCursor),
                StandardCharsets.UTF_8
            );
            int separator = value.lastIndexOf('|');
            if (separator <= 0 || separator == value.length() - 1) {
                throw new IllegalArgumentException("Invalid cursor");
            }

            return new InstantUuidCursor(
                Instant.parse(value.substring(0, separator)),
                UUID.fromString(value.substring(separator + 1))
            );
        } catch (IllegalArgumentException exception) {
            throw new ValidationException(Map.of(
                "cursor",
                "Cursor de paginação inválido."
            ));
        }
    }
}
