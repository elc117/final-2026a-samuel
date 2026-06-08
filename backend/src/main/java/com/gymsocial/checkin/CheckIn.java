package com.gymsocial.checkin;

import java.time.Instant;
import java.util.UUID;

public record CheckIn(
    UUID id,
    UUID groupId,
    Long authorUserId,
    String title,
    String description,
    String imageUrl,
    Instant createdAt
) {
}
