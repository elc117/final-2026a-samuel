package com.gymsocial.chat.dto;

import java.util.UUID;

public record ChatSessionResponse(
    UUID groupId,
    String userCode,
    String userName,
    String userImageUrl
) {
}

