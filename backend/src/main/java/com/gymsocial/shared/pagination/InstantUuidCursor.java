package com.gymsocial.shared.pagination;

import java.time.Instant;
import java.util.UUID;

public record InstantUuidCursor(Instant createdAt, UUID id) {
}
