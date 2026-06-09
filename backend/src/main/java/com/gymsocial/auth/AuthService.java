package com.gymsocial.auth;

import com.gymsocial.auth.dto.AuthResponse;
import com.gymsocial.auth.dto.LoginRequest;
import com.gymsocial.auth.dto.RegisterRequest;
import com.gymsocial.auth.dto.UserResponse;
import com.gymsocial.shared.exception.ConflictException;
import com.gymsocial.shared.exception.UnauthorizedException;
import com.gymsocial.shared.storage.ImageStorage;
import com.gymsocial.shared.id.PublicIdCodec;
import com.gymsocial.shared.validation.RequestValidator;
import com.gymsocial.user.User;
import com.gymsocial.user.UserRepository;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public final class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RequestValidator requestValidator;
    private final ImageStorage imageStorage;
    private final PublicIdCodec publicIdCodec;

    public AuthService(
        UserRepository userRepository,
        PasswordHasher passwordHasher,
        JwtService jwtService,
        RefreshTokenService refreshTokenService,
        RefreshTokenRepository refreshTokenRepository,
        RequestValidator requestValidator,
        ImageStorage imageStorage,
        PublicIdCodec publicIdCodec
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.requestValidator = requestValidator;
        this.imageStorage = imageStorage;
        this.publicIdCodec = publicIdCodec;
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

    public AuthResult refresh(String currentRefreshToken) {
        if (currentRefreshToken == null || currentRefreshToken.isBlank()) {
            throw new UnauthorizedException("Sessão expirada. Entre novamente.");
        }

        Instant now = Instant.now();
        UUID replacementId = UUID.randomUUID();
        String replacementToken = refreshTokenService.generateToken();
        String replacementHash = refreshTokenService.hash(replacementToken);

        long userId = refreshTokenRepository.rotate(
            refreshTokenService.hash(currentRefreshToken),
            replacementId,
            replacementHash,
            now,
            now.plus(RefreshTokenService.REFRESH_TOKEN_DURATION)
        ).orElseThrow(() ->
            new UnauthorizedException("Sessão expirada. Entre novamente.")
        );

        User user = userRepository.findById(userId)
            .filter(found -> "ACTIVE".equals(found.status()))
            .orElseThrow(() ->
                new UnauthorizedException("Sessão expirada. Entre novamente.")
            );

        return new AuthResult(
            jwtService.createAccessToken(user),
            replacementToken,
            toUserResponse(user)
        );
    }

    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        refreshTokenRepository.revoke(
            refreshTokenService.hash(refreshToken),
            Instant.now()
        );
    }

    public UserResponse currentUser(long userId) {
        return userRepository.findById(userId)
            .filter(user -> "ACTIVE".equals(user.status()))
            .map(this::toUserResponse)
            .orElseThrow(() ->
                new UnauthorizedException("Usuário autenticado não encontrado.")
            );
    }

    private AuthResult authenticated(User user) {
        Instant now = Instant.now();
        String refreshToken = refreshTokenService.generateToken();

        refreshTokenRepository.create(
            UUID.randomUUID(),
            user.id(),
            refreshTokenService.hash(refreshToken),
            now,
            now.plus(RefreshTokenService.REFRESH_TOKEN_DURATION)
        );

        return new AuthResult(
            jwtService.createAccessToken(user),
            refreshToken,
            toUserResponse(user)
        );
    }

    private UserResponse toUserResponse(User user) {
        String profileImageUrl = user.profileImageUrl() == null
            ? null
            : imageStorage.createReadUrl(user.profileImageUrl());

        return UserResponse.from(
            user,
            publicIdCodec.encode(user.id()),
            profileImageUrl
        );
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public record AuthResult(
        String accessToken,
        String refreshToken,
        UserResponse user
    ) {
        public AuthResponse response() {
            return new AuthResponse(accessToken, user);
        }
    }
}
