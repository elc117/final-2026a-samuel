package com.gymsocial.auth.dto;

import com.gymsocial.user.User;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String name,
    String username,
    String email,
    String profileImageUrl,
    String createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
            user.id(),
            user.name(),
            user.username(),
            user.email(),
            user.profileImageUrl(),
            user.createdAt().toString()
        );
    }
}
