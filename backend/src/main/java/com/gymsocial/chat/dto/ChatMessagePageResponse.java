package com.gymsocial.chat.dto;

import java.util.List;

public record ChatMessagePageResponse(
    List<ChatMessageResponse> items,
    String nextCursor,
    boolean hasMore
) {
}

