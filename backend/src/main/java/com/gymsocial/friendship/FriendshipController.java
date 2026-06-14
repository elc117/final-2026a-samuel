package com.gymsocial.friendship;

import com.gymsocial.shared.exception.NotFoundException;
import com.gymsocial.shared.exception.ValidationException;
import com.gymsocial.shared.http.AuthenticatedUserContext;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class FriendshipController {

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

    public void list(Context context) {
        context.json(service.findFriends(
            AuthenticatedUserContext.getUserId(context),
            context.queryParam("cursor"),
            parseLimit(context)
        ));
    }

    public void incomingCount(Context context) {
        context.json(new FriendshipRequestCountResponse(
            service.countIncoming(
                AuthenticatedUserContext.getUserId(context)
            )
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
        }
        catch (IllegalArgumentException exception) {
            throw new NotFoundException(
                "Solicitação de conexão não encontrada."
            );
        }
    }

    private int parseLimit(Context context) {
        String value = context.queryParam("limit");
        if (value == null || value.isBlank()) {
            return 20;
        }

        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException exception) {
            throw new ValidationException(Map.of(
                "limit",
                "Informe um limite válido."
            ));
        }
    }

    private record FriendshipRequestCountResponse(int count) {
    }
}
