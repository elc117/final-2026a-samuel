package com.gymsocial.config;

import com.gymsocial.checkin.CheckInController;
import com.gymsocial.group.GroupController;
import com.gymsocial.group.invitation.GroupInvitationController;
import com.gymsocial.user.UserProfileController;
import io.javalin.config.JavalinConfig;

public final class RouteConfig {

    private RouteConfig() {
    }

    public static void register(
        JavalinConfig config,
        AuthModule.Components auth,
        GroupController groupController,
        GroupInvitationController groupInvitationController,
        UserProfileController userProfileController,
        CheckInController checkInController
    ) {
        config.routes.get("/health", context ->
            context
                .contentType("application/json")
                .result("{\"status\":\"UP\"}")
        );
        config.routes.post("/auth/register", auth.controller()::register);
        config.routes.post("/auth/login", auth.controller()::login);
        config.routes.post("/auth/refresh", auth.controller()::refresh);
        config.routes.post("/auth/logout", auth.controller()::logout);

        config.routes.before("/auth/me", auth.middleware()::authenticate);
        config.routes.get("/auth/me", auth.controller()::me);

        config.routes.before("/groups", auth.middleware()::authenticate);
        config.routes.before("/groups/*", auth.middleware()::authenticate);
        config.routes.get("/groups/me", groupController::current);
        config.routes.post("/groups", groupController::create);
        config.routes.get(
            "/groups/{groupId}/invite-link",
            groupInvitationController::findLink
        );

        config.routes.before(
            "/group-invitations/*",
            auth.middleware()::authenticate
        );
        config.routes.get(
            "/group-invitations/{token}",
            groupInvitationController::findByToken
        );
        config.routes.post(
            "/group-invitations/{token}/accept",
            groupInvitationController::accept
        );

        config.routes.before("/users/me", auth.middleware()::authenticate);
        config.routes.get("/users/me", userProfileController::current);
        config.routes.put("/users/me", userProfileController::update);

        config.routes.before("/check-ins", auth.middleware()::authenticate);
        config.routes.get("/check-ins", checkInController::list);
        config.routes.post("/check-ins", checkInController::create);
    }
}
