package com.gymsocial;

import io.javalin.Javalin;

public final class Application {

    private static final int DEFAULT_PORT = 7000;

    private Application() {
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(
            System.getenv().getOrDefault("APP_PORT", String.valueOf(DEFAULT_PORT))
        );

        Javalin.create(config ->
            config.routes.get("/health", context ->
                context
                    .contentType("application/json")
                    .result("{\"status\":\"UP\"}")
            )
        ).start(port);
    }
}
