package com.gymsocial.config;

import com.gymsocial.auth.AuthController;
import com.gymsocial.auth.AuthService;
import com.gymsocial.auth.JwtService;
import com.gymsocial.auth.PasswordHasher;
import com.gymsocial.shared.validation.RequestValidator;
import com.gymsocial.user.UserRepository;

import javax.sql.DataSource;

public final class AuthModule {

    private AuthModule() {
    }

    public static AuthController create(DataSource dataSource, ApplicationConfig appConfig) {
        var userRepository = new UserRepository(dataSource);
        var authService = new AuthService(
            userRepository,
            new PasswordHasher(),
            new JwtService(appConfig.jwtSecret()),
            new RequestValidator()
        );

        return new AuthController(authService, appConfig.cookieSecure());
    }
}
