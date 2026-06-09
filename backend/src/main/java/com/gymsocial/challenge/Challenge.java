package com.gymsocial.challenge;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record Challenge(
    UUID id,
    UUID groupId,
    long creatorUserId,
    String title,
    String description,
    String period,
    boolean allowMultipleCheckInsPerDay,
    LocalDate startsAt,
    LocalDate endsAt,
    String status,
    Instant createdAt
) {
}
