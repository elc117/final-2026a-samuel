package com.gymsocial.auth;

import com.gymsocial.auth.dto.LoginRequest;
import com.gymsocial.auth.dto.RegisterRequest;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public final class AuthController {

    private final AuthService authService;
    private final boolean cookieSecure;

    public AuthController(AuthService authService, boolean cookieSecure) {
        this.authService = authService;
        this.cookieSecure = cookieSecure;
    }

    public void register(Context context) {
        AuthService.AuthResult result = authService.register(
            context.bodyAsClass(RegisterRequest.class)
        );

        setAccessTokenCookie(context, result.accessToken());
        context.status(HttpStatus.CREATED).json(result.response());
    }

    public void login(Context context) {
        AuthService.AuthResult result = authService.login(
            context.bodyAsClass(LoginRequest.class)
        );

        setAccessTokenCookie(context, result.accessToken());
        context.json(result.response());
    }

    private void setAccessTokenCookie(Context context, String token) {
        long maxAge = JwtService.ACCESS_TOKEN_DURATION.toSeconds();
        String secureAttribute = cookieSecure ? "; Secure" : "";

        context.header(
            "Set-Cookie",
            JwtService.ACCESS_TOKEN_COOKIE + "=" + token +
                "; Path=/; Max-Age=" + maxAge +
                "; HttpOnly; SameSite=Lax" +
                secureAttribute
        );
    }
}
