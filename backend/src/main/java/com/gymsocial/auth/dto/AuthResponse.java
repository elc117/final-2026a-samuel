package com.gymsocial.auth.dto;

public record AuthResponse(
    String accessToken,
    UserResponse user
) {
}
