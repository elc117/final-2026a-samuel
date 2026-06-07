package com.gymsocial.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gymsocial.shared.exception.ConflictException;
import com.gymsocial.shared.exception.UnauthorizedException;
import com.gymsocial.shared.exception.ValidationException;
import com.gymsocial.shared.response.ErrorResponse;
import io.javalin.config.JavalinConfig;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.HttpStatus;

public final class ExceptionConfig {

    private static final String INVALID_BODY_MESSAGE =
        "Corpo da requisição inválido.";

    private ExceptionConfig() {
    }

    public static void register(JavalinConfig config) {
        config.routes.exception(ValidationException.class, (exception, context) ->
            context
                .status(HttpStatus.BAD_REQUEST)
                .json(new ErrorResponse(
                    exception.getMessage(),
                    exception.errors()
                ))
        );
        config.routes.exception(ConflictException.class, (exception, context) ->
            context
                .status(HttpStatus.CONFLICT)
                .json(new ErrorResponse(exception.getMessage()))
        );
        config.routes.exception(UnauthorizedException.class, (exception, context) ->
            context
                .status(HttpStatus.UNAUTHORIZED)
                .json(new ErrorResponse(exception.getMessage()))
        );
        config.routes.exception(BadRequestResponse.class, (exception, context) ->
            context
                .status(HttpStatus.BAD_REQUEST)
                .json(new ErrorResponse(INVALID_BODY_MESSAGE))
        );
        config.routes.exception(JsonProcessingException.class, (exception, context) ->
            context
                .status(HttpStatus.BAD_REQUEST)
                .json(new ErrorResponse(INVALID_BODY_MESSAGE))
        );
    }
}
