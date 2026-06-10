package com.gymsocial.group.dto;

import java.time.Instant;

public record GroupMemberResponse(
    String code,
    String name,
    String username,
    String profileImageUrl,
    boolean administrator,
    Instant joinedAt
) {
}
