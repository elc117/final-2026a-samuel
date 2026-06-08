package com.gymsocial.config;

import com.gymsocial.checkin.CheckInController;
import com.gymsocial.group.GroupController;
import com.gymsocial.group.invitation.GroupInvitationController;
import com.gymsocial.user.UserProfileController;
import com.zaxxer.hikari.HikariDataSource;
import io.javalin.Javalin;
import io.javalin.config.SizeUnit;

public final class JavalinConfig {

    private JavalinConfig() {
    }

    public static Javalin create(
        ApplicationConfig appConfig,
        HikariDataSource dataSource,
        AuthModule.Components auth,
        GroupController groupController,
        GroupInvitationController groupInvitationController,
        UserProfileController userProfileController,
        CheckInController checkInController
    ) {
        return Javalin.create(config -> {
            config.jetty.multipartConfig.maxFileSize(2L, SizeUnit.MB);
            config.jetty.multipartConfig.maxInMemoryFileSize(2, SizeUnit.MB);
            config.jetty.multipartConfig.maxTotalRequestSize(3L, SizeUnit.MB);
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

            RouteConfig.register(
                config,
                auth,
                groupController,
                groupInvitationController,
                userProfileController,
                checkInController
            );
            ExceptionConfig.register(config);
        });
    }
}
