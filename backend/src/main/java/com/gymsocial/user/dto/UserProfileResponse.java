package com.gymsocial.user.dto;

import com.gymsocial.user.User;

public record UserProfileResponse(
    String code,
    String name,
    String username,
    String profileImageUrl,
    int friendCount
) {

    public static UserProfileResponse from(
        User user,
        String code,
        String profileImageUrl,
        int friendCount
    ) {
        return new UserProfileResponse(
            code,
            user.name(),
            user.username(),
            profileImageUrl,
            friendCount
        );
    }
}
