package com.gymsocial.chat.dto;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageResponse(
    UUID id,
    UUID groupId,
    String authorCode,
    String authorName,
    String authorImageUrl,
    String content,
    Instant createdAt
) {
}

