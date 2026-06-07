package com.gymsocial.config;

import com.gymsocial.group.GroupController;
import com.zaxxer.hikari.HikariDataSource;
import io.javalin.Javalin;

public final class JavalinConfig {

    private JavalinConfig() {
    }

    public static Javalin create(
        ApplicationConfig appConfig,
        HikariDataSource dataSource,
        AuthModule.Components auth,
        GroupController groupController
    ) {
        return Javalin.create(config -> {
            config.events.serverStopped(dataSource::close);
            config.bundledPlugins.enableCors(cors -> {
                for (
                    String allowedOrigin :
                    appConfig.corsAllowedOrigin().split(",")
                ) {
                    cors.addRule(rule -> {
                        rule.allowHost(allowedOrigin.trim());
                        rule.allowCredentials = true;
                    });
                }
            });

            RouteConfig.register(config, auth, groupController);
            ExceptionConfig.register(config);
        });
    }
}
