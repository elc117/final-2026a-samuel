package com.gymsocial.shared.http;

import com.gymsocial.shared.exception.UnauthorizedException;
import io.javalin.http.Context;

public final class AuthenticatedUserContext {

    private static final String USER_ID_ATTRIBUTE = "authenticatedUserId";
    private static final String UNAUTHORIZED_MESSAGE =
        "Token de acesso ausente ou inválido.";

    private AuthenticatedUserContext() {
    }

    public static void setUserId(Context context, long userId) {

        context.attribute(USER_ID_ATTRIBUTE, userId);

    }

    public static long getUserId(Context context) {
        Long userId = context.attribute(USER_ID_ATTRIBUTE);

        if (userId == null) {
            throw new UnauthorizedException(UNAUTHORIZED_MESSAGE);
        }

        return userId;
    }
}
