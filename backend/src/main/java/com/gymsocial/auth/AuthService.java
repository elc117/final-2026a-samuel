package com.gymsocial.auth;

import com.gymsocial.auth.dto.AuthResponse;
import com.gymsocial.auth.dto.LoginRequest;
import com.gymsocial.auth.dto.RegisterRequest;
import com.gymsocial.auth.dto.UserResponse;
import com.gymsocial.shared.exception.ConflictException;
import com.gymsocial.shared.exception.UnauthorizedException;
import com.gymsocial.shared.validation.RequestValidator;
import com.gymsocial.user.User;
import com.gymsocial.user.UserRepository;

import java.time.Instant;
import java.util.Locale;

public final class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;
    private final RequestValidator requestValidator;

    public AuthService(
        UserRepository userRepository,
        PasswordHasher passwordHasher,
        JwtService jwtService,
        RequestValidator requestValidator
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
        this.requestValidator = requestValidator;
    }

    public AuthResult register(RegisterRequest request) {
        requestValidator.validate(request);

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
            null,
            request.name().trim(),
            username,
            email,
            passwordHasher.hash(request.password()),
            null,
            "ACTIVE",
            now,
            now
        );

        User savedUser = userRepository.save(user);
        return authenticated(savedUser);
    }

    public AuthResult login(LoginRequest request) {
        requestValidator.validate(request);

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
