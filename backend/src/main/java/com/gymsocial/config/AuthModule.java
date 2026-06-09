package com.gymsocial.config;

import com.gymsocial.auth.AuthController;
import com.gymsocial.auth.AuthService;
import com.gymsocial.auth.JwtService;
import com.gymsocial.auth.PasswordHasher;
import com.gymsocial.auth.RefreshTokenRepository;
import com.gymsocial.auth.RefreshTokenService;
import com.gymsocial.config.middleware.JwtAuthenticationMiddleware;
import com.gymsocial.shared.validation.RequestValidator;
import com.gymsocial.shared.storage.ImageStorage;
import com.gymsocial.shared.id.PublicIdCodec;
import com.gymsocial.user.UserRepository;

import javax.sql.DataSource;

public final class AuthModule {

    private AuthModule() {
    }

    public static Components create(
        DataSource dataSource,
        ApplicationConfig appConfig,
        ImageStorage imageStorage,
        PublicIdCodec publicIdCodec
    ) {
        var userRepository = new UserRepository(dataSource);

        var jwtService = new JwtService(
            appConfig.jwtSecret(),
            publicIdCodec
        );

        var authService = new AuthService(
            userRepository,
            new PasswordHasher(),
            jwtService,
            new RefreshTokenService(),
            new RefreshTokenRepository(dataSource),
            new RequestValidator(),
            imageStorage,
            publicIdCodec
        );

        return new Components(
            new AuthController(authService, appConfig.cookieSecure()),
            new JwtAuthenticationMiddleware(jwtService)
        );
    }

    public record Components(AuthController controller, JwtAuthenticationMiddleware middleware) {
    }
}
