package com.gymsocial.group.dto;

import com.gymsocial.group.Group;

import java.util.UUID;

public record GroupResponse(
    UUID id,
    String adminUserCode,
    String name,
    String imageUrl,
    int memberCount,
    String createdAt
) {

    public static GroupResponse from(
        Group group,
        String adminUserCode,
        String imageUrl,
        int memberCount
    ) {
        return new GroupResponse(
            group.id(),
            adminUserCode,
            group.name(),
            imageUrl,
            memberCount,
            group.createdAt().toString()
        );
    }
}
