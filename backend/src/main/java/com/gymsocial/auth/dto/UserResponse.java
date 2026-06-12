package com.gymsocial.auth.dto;

import com.gymsocial.user.User;

public record UserResponse(
    String code,
    String name,
    String username,
    String email,
    String profileImageUrl,
    String createdAt
) {

    public static UserResponse from(User user, String code, String profileImageUrl) {
        return new UserResponse(
            code,
            user.name(),
            user.username(),
            user.email(),
            profileImageUrl,
            user.createdAt().toString()
        );
    }
}
