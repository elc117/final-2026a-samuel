package com.gymsocial.config.middleware;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.gymsocial.auth.JwtService;
import com.gymsocial.shared.exception.UnauthorizedException;
import com.gymsocial.shared.http.AuthenticatedUserContext;
import io.javalin.http.Context;

public final class JwtAuthenticationMiddleware {

    private static final String UNAUTHORIZED_MESSAGE =
        "Token de acesso ausente ou inválido.";

    private final JwtService jwtService;

    public JwtAuthenticationMiddleware(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void authenticate(Context context) {
        String token = context.cookie(JwtService.ACCESS_TOKEN_COOKIE);

        if (token == null || token.isBlank()) {
            throw new UnauthorizedException(UNAUTHORIZED_MESSAGE);
        }

        try {
            long userId = jwtService.verifyAccessToken(token);
            AuthenticatedUserContext.setUserId(context, userId);
        }
        catch (JWTVerificationException | IllegalArgumentException exception) {
            throw new UnauthorizedException(UNAUTHORIZED_MESSAGE);
        }
    }
}
