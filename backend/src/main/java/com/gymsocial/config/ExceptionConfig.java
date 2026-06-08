package com.gymsocial.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gymsocial.shared.exception.ConflictException;
import com.gymsocial.shared.exception.ForbiddenException;
import com.gymsocial.shared.exception.NotFoundException;
import com.gymsocial.shared.exception.UnauthorizedException;
import com.gymsocial.shared.exception.ValidationException;
import com.gymsocial.shared.response.ErrorResponse;
import io.javalin.config.JavalinConfig;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExceptionConfig {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(ExceptionConfig.class);
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
        config.routes.exception(ForbiddenException.class, (exception, context) ->
            context
                .status(HttpStatus.FORBIDDEN)
                .json(new ErrorResponse(exception.getMessage()))
        );
        config.routes.exception(NotFoundException.class, (exception, context) ->
            context
                .status(HttpStatus.NOT_FOUND)
                .json(new ErrorResponse(exception.getMessage()))
        );
        config.routes.exception(UnauthorizedException.class, (exception, context) ->
            context
                .status(HttpStatus.UNAUTHORIZED)
                .json(new ErrorResponse(exception.getMessage()))
        );
        config.routes.exception(BadRequestResponse.class, (exception, context) -> {
            LOGGER.warn(
                "Invalid request body on {} {}: {}",
                context.method(),
                context.path(),
                exception.getMessage()
            );
            context
                .status(HttpStatus.BAD_REQUEST)
                .json(new ErrorResponse(INVALID_BODY_MESSAGE));
        });
        config.routes.exception(JsonProcessingException.class, (exception, context) -> {
            LOGGER.warn(
                "Could not parse JSON on {} {}: {}",
                context.method(),
                context.path(),
                exception.getOriginalMessage()
            );
            context
                .status(HttpStatus.BAD_REQUEST)
                .json(new ErrorResponse(INVALID_BODY_MESSAGE));
        });
    }
}
