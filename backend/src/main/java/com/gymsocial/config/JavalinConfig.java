package com.gymsocial.config;

import com.zaxxer.hikari.HikariDataSource;
import io.javalin.Javalin;

public final class JavalinConfig {

    private JavalinConfig() {
    }

    public static Javalin create(
        ApplicationConfig appConfig,
        HikariDataSource dataSource,
        AuthModule.Components auth
    ) {
        return Javalin.create(config -> {
            config.events.serverStopped(dataSource::close);
            config.bundledPlugins.enableCors(cors ->
                cors.addRule(rule -> {
                    rule.allowHost(appConfig.corsAllowedOrigin());
                    rule.allowCredentials = true;
                })
            );

            RouteConfig.register(config, auth);
            ExceptionConfig.register(config);
        });
    }
}
