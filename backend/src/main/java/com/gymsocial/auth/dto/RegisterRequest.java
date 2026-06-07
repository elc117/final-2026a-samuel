package com.gymsocial.auth.dto;

public record RegisterRequest(
    String name,
    String username,
    String email,
    String password
) {
}
