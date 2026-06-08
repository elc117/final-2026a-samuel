package com.gymsocial.user.dto;

import com.gymsocial.user.User;

public record UserProfileResponse(
    Long id,
    String name,
    String username,
    String profileImageUrl,
    int friendCount
) {

    public static UserProfileResponse from(
        User user,
        String profileImageUrl,
        int friendCount
    ) {
        return new UserProfileResponse(
            user.id(),
            user.name(),
            user.username(),
            profileImageUrl,
            friendCount
        );
    }
}
