package com.gymsocial.config.middleware;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.gymsocial.auth.JwtService;
import com.gymsocial.shared.exception.UnauthorizedException;
import com.gymsocial.shared.http.AuthenticatedUserContext;
import io.javalin.http.Context;

public final class JwtAuthenticationMiddleware {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String UNAUTHORIZED_MESSAGE =
        "Token de acesso ausente ou inválido.";

    private final JwtService jwtService;

    public JwtAuthenticationMiddleware(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void authenticate(Context context) {
        String authorization = context.header("Authorization");

        if (
            authorization == null ||
            !authorization.startsWith(BEARER_PREFIX)
        ) {
            throw new UnauthorizedException(UNAUTHORIZED_MESSAGE);
        }

        try {
            String token = authorization.substring(BEARER_PREFIX.length());

            if (token.isBlank()) {
                throw new IllegalArgumentException("Bearer token is missing");
            }

            long userId = jwtService.verifyAccessToken(token);
            AuthenticatedUserContext.setUserId(context, userId);
        }
        catch (JWTVerificationException | IllegalArgumentException exception) {
            throw new UnauthorizedException(UNAUTHORIZED_MESSAGE);
        }
    }
}
