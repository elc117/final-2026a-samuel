package com.gymsocial.checkin.dto;

import com.gymsocial.checkin.CheckIn;

import java.util.UUID;

public record CheckInResponse(
    UUID id,
    UUID groupId,
    Long authorUserId,
    String authorName,
    String authorImageUrl,
    String title,
    String description,
    String imageUrl,
    String createdAt
) {

    public static CheckInResponse from(
        CheckIn checkIn,
        String authorName,
        String authorImageUrl,
        String imageUrl
    ) {
        return new CheckInResponse(
            checkIn.id(),
            checkIn.groupId(),
            checkIn.authorUserId(),
            authorName,
            authorImageUrl,
            checkIn.title(),
            checkIn.description(),
            imageUrl,
            checkIn.createdAt().toString()
        );
    }
}
