package com.gymsocial.auth.dto;

public record LoginRequest(
    String email,
    String password
) {
}
