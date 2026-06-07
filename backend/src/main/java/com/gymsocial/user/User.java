package com.gymsocial.user;

import java.time.Instant;

public record User(
    Long id,
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
