package com.gymsocial.group;

import java.time.Instant;
import java.util.UUID;

public record Group(
    UUID id,
    Long adminUserId,
    String name,
    String imageUrl,
    Instant createdAt,
    Instant updatedAt
) {
}
