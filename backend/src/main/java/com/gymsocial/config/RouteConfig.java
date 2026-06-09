package com.gymsocial.config;

import com.gymsocial.challenge.ChallengeController;
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
        CheckInModule.Components checkIn,
        ChallengeController challengeController
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
        config.routes.before("/users/*", auth.middleware()::authenticate);
        config.routes.get("/users/me", userProfileController::current);
        config.routes.put("/users/me", userProfileController::update);
        config.routes.get("/users/{userId}", userProfileController::find);

        config.routes.before("/check-ins", auth.middleware()::authenticate);
        config.routes.before("/check-ins/*", auth.middleware()::authenticate);
        config.routes.get("/check-ins", checkIn.checkInController()::list);
        config.routes.post("/check-ins", checkIn.checkInController()::create);
        config.routes.get(
            "/check-ins/{checkInId}/comments",
            checkIn.commentController()::list
        );
        config.routes.post(
            "/check-ins/{checkInId}/comments",
            checkIn.commentController()::create
        );

        config.routes.before("/challenges", auth.middleware()::authenticate);
        config.routes.before("/challenges/*", auth.middleware()::authenticate);
        config.routes.get("/challenges/current", challengeController::current);
        config.routes.post("/challenges", challengeController::create);
        config.routes.delete(
            "/challenges/current",
            challengeController::endCurrent
        );
    }
}
