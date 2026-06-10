package com.gymsocial.checkin.dto;

import java.util.List;

public record CheckInPageResponse(
    List<CheckInResponse> items,
    String nextCursor,
    boolean hasMore
) {
}
