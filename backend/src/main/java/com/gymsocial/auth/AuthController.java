package com.gymsocial.auth;

import com.gymsocial.auth.dto.LoginRequest;
import com.gymsocial.auth.dto.RegisterRequest;
import com.gymsocial.shared.http.AuthenticatedUserContext;
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

        setRefreshTokenCookie(context, result.refreshToken());
        context.status(HttpStatus.CREATED).json(result.response());
    }

    public void login(Context context) {
        AuthService.AuthResult result = authService.login(
            context.bodyAsClass(LoginRequest.class)
        );

        setRefreshTokenCookie(context, result.refreshToken());
        context.json(result.response());
    }

    public void refresh(Context context) {
        AuthService.AuthResult result = authService.refresh(
            context.cookie(RefreshTokenService.REFRESH_TOKEN_COOKIE)
        );

        setRefreshTokenCookie(context, result.refreshToken());
        context.json(result.response());
    }

    public void logout(Context context) {
        authService.logout(
            context.cookie(RefreshTokenService.REFRESH_TOKEN_COOKIE)
        );
        clearRefreshTokenCookie(context);
        context.status(HttpStatus.NO_CONTENT);
    }

    public void me(Context context) {
        long userId = AuthenticatedUserContext.getUserId(context);
        context.json(authService.currentUser(userId));
    }

    private void setRefreshTokenCookie(Context context, String refreshToken) {
        addRefreshTokenCookie(
                context,
                refreshToken,
                RefreshTokenService.REFRESH_TOKEN_DURATION.toSeconds()
        );
    }

    private void clearRefreshTokenCookie(Context context) {
        addRefreshTokenCookie(context, "", 0);
    }

    private void addRefreshTokenCookie(Context context, String value, long maxAge) {
        String secureAttribute = cookieSecure ? "; Secure" : "";

        context.res().addHeader(
                "Set-Cookie",
                RefreshTokenService.REFRESH_TOKEN_COOKIE + "=" + value +
                        "; Path=/" +
                        "; Max-Age=" + maxAge +
                        "; HttpOnly; SameSite=Lax" +
                        secureAttribute
        );
    }
}
