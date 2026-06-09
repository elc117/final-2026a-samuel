package com.gymsocial.friendship;

import com.gymsocial.shared.exception.NotFoundException;
import com.gymsocial.shared.http.AuthenticatedUserContext;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.UUID;

public final class FriendshipController {

    private final FriendshipService service;

    public FriendshipController(FriendshipService service) {
        this.service = service;
    }

    public void request(Context context) {
        service.request(
            AuthenticatedUserContext.getUserId(context),
            context.pathParam("userCode")
        );
        context.status(HttpStatus.NO_CONTENT);
    }

    public void incoming(Context context) {
        context.json(service.findIncoming(
            AuthenticatedUserContext.getUserId(context)
        ));
    }

    public void accept(Context context) {
        service.accept(
            AuthenticatedUserContext.getUserId(context),
            parseId(context)
        );
        context.status(HttpStatus.NO_CONTENT);
    }

    public void reject(Context context) {
        service.reject(
            AuthenticatedUserContext.getUserId(context),
            parseId(context)
        );
        context.status(HttpStatus.NO_CONTENT);
    }

    private UUID parseId(Context context) {
        try {
            return UUID.fromString(context.pathParam("friendshipId"));
        } catch (IllegalArgumentException exception) {
            throw new NotFoundException(
                "Solicitação de conexão não encontrada."
            );
        }
    }
}
