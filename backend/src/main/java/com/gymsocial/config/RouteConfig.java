package com.gymsocial.config;

import com.gymsocial.auth.AuthController;
import io.javalin.config.JavalinConfig;

public final class RouteConfig {

    private RouteConfig() {
    }

    public static void register(JavalinConfig config, AuthController authController) {
        config.routes.get("/health", context ->
            context
                .contentType("application/json")
                .result("{\"status\":\"UP\"}")
        );
        config.routes.post("/auth/register", authController::register);
        config.routes.post("/auth/login", authController::login);
    }
}
