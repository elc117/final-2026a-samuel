package com.gymsocial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gymsocial.auth.AuthController;
import com.gymsocial.auth.AuthService;
import com.gymsocial.auth.JwtService;
import com.gymsocial.auth.PasswordHasher;
import com.gymsocial.config.ApplicationConfig;
import com.gymsocial.config.DatabaseConfig;
import com.gymsocial.shared.exception.ConflictException;
import com.gymsocial.shared.exception.UnauthorizedException;
import com.gymsocial.shared.exception.ValidationException;
import com.gymsocial.shared.response.ErrorResponse;
import com.gymsocial.user.UserRepository;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.HttpStatus;

public final class Application {

    private Application() {
    }

    public static void main(String[] args) {
        ApplicationConfig appConfig = ApplicationConfig.fromEnvironment();
        var dataSource = DatabaseConfig.createDataSource(appConfig);
        var userRepository = new UserRepository(dataSource);
        var authService = new AuthService(
            userRepository,
            new PasswordHasher(),
            new JwtService(appConfig.jwtSecret())
        );
        var authController = new AuthController(
            authService,
            appConfig.cookieSecure()
        );

        Javalin.create(config -> {
            config.events.serverStopped(dataSource::close);
            config.bundledPlugins.enableCors(cors ->
                cors.addRule(rule -> {
                    rule.allowHost(appConfig.corsAllowedOrigin());
                    rule.allowCredentials = true;
                })
            );

            config.routes.get("/health", context ->
                context
                    .contentType("application/json")
                    .result("{\"status\":\"UP\"}")
            );
            config.routes.post("/auth/register", authController::register);
            config.routes.post("/auth/login", authController::login);

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
                    .json(new ErrorResponse("Corpo da requisição inválido."))
            );
            config.routes.exception(JsonProcessingException.class, (exception, context) ->
                context
                    .status(HttpStatus.BAD_REQUEST)
                    .json(new ErrorResponse("Corpo da requisição inválido."))
            );
        })
            .start(appConfig.port());
    }
}
