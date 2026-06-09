package com.gymsocial.challenge;

import com.gymsocial.challenge.dto.CreateChallengeRequest;
import com.gymsocial.shared.http.AuthenticatedUserContext;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public final class ChallengeController {

    private final ChallengeService service;

    public ChallengeController(ChallengeService service) {
        this.service = service;
    }

    public void current(Context context) {
        long userId = AuthenticatedUserContext.getUserId(context);
        service.findCurrent(userId).ifPresentOrElse(
            context::json,
            () -> context.status(HttpStatus.NO_CONTENT)
        );
    }

    public void create(Context context) {
        long userId = AuthenticatedUserContext.getUserId(context);
        context
            .status(HttpStatus.CREATED)
            .json(service.create(
                userId,
                context.bodyAsClass(CreateChallengeRequest.class)
            ));
    }

    public void endCurrent(Context context) {
        long userId = AuthenticatedUserContext.getUserId(context);
        service.endCurrent(userId);
        context.status(HttpStatus.NO_CONTENT);
    }
}
