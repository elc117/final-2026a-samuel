package com.gymsocial.auth;

import com.gymsocial.auth.dto.AuthResponse;
import com.gymsocial.auth.dto.LoginRequest;
import com.gymsocial.auth.dto.RegisterRequest;
import com.gymsocial.auth.dto.UserResponse;
import com.gymsocial.shared.exception.ConflictException;
import com.gymsocial.shared.exception.UnauthorizedException;
import com.gymsocial.user.User;
import com.gymsocial.user.UserRepository;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public final class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        PasswordHasher passwordHasher,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
    }

    public AuthResult register(RegisterRequest request) {
        AuthValidator.validate(request);

        String email = normalize(request.email());
        String username = normalize(request.username());

        if (
            userRepository.existsByEmail(email) ||
            userRepository.existsByUsername(username)
        ) {
            throw new ConflictException("E-mail ou usuário já cadastrado.");
        }

        Instant now = Instant.now();
        User user = new User(
            UUID.randomUUID(),
            request.name().trim(),
            username,
            email,
            passwordHasher.hash(request.password()),
            null,
            "ACTIVE",
            now,
            now
        );

        userRepository.save(user);
        return authenticated(user);
    }

    public AuthResult login(LoginRequest request) {
        AuthValidator.validate(request);

        User user = userRepository.findByEmail(normalize(request.email()))
            .filter(found -> "ACTIVE".equals(found.status()))
            .filter(found -> passwordHasher.matches(
                request.password(),
                found.passwordHash()
            ))
            .orElseThrow(() ->
                new UnauthorizedException("E-mail ou senha inválidos.")
            );

        return authenticated(user);
    }

    private AuthResult authenticated(User user) {
        return new AuthResult(
            jwtService.createAccessToken(user),
            new AuthResponse(UserResponse.from(user))
        );
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public record AuthResult(
        String accessToken,
        AuthResponse response
    ) {
    }
}
