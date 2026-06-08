package com.gymsocial.auth.dto;

import com.gymsocial.user.User;

public record UserResponse(
    Long id,
    String name,
    String username,
    String email,
    String profileImageUrl,
    String createdAt
) {

    public static UserResponse from(User user) {
        return from(user, user.profileImageUrl());
    }

    public static UserResponse from(User user, String profileImageUrl) {
        return new UserResponse(
            user.id(),
            user.name(),
            user.username(),
            user.email(),
            profileImageUrl,
            user.createdAt().toString()
        );
    }
}
