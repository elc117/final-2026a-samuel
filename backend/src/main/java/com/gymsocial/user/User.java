package com.gymsocial.user;

import java.time.Instant;
import java.util.UUID;

public record User(
    UUID id,
    String name,
    String username,
    String email,
    String passwordHash,
    String profileImageUrl,
    String status,
    Instant createdAt,
    Instant updatedAt
) {
}
