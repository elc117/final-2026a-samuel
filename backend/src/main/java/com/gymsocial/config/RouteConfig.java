package com.gymsocial.config;

import io.javalin.config.JavalinConfig;

public final class RouteConfig {

    private RouteConfig() {
    }

    public static void register(
        JavalinConfig config,
        AuthModule.Components auth
    ) {
        config.routes.get("/health", context ->
            context
                .contentType("application/json")
                .result("{\"status\":\"UP\"}")
        );
        config.routes.post("/auth/register", auth.controller()::register);
        config.routes.post("/auth/login", auth.controller()::login);

        config.routes.before("/auth/me", auth.middleware()::authenticate);
        config.routes.get("/auth/me", auth.controller()::me);
    }
}
