package com.gymsocial.group.invitation;

import com.gymsocial.shared.exception.NotFoundException;
import com.gymsocial.shared.http.AuthenticatedUserContext;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.UUID;

public final class GroupInvitationController {

    private final GroupInvitationService service;

    public GroupInvitationController(GroupInvitationService service) {
        this.service = service;
    }

    public void findLink(Context context) {
        long userId = AuthenticatedUserContext.getUserId(context);
        UUID groupId = parseUuid(context.pathParam("groupId"));

        context.json(service.findLink(groupId, userId));
    }

    public void findByToken(Context context) {
        long userId = AuthenticatedUserContext.getUserId(context);
        UUID token = parseUuid(context.pathParam("token"));

        context.json(service.findByToken(token, userId));
    }

    public void accept(Context context) {
        long userId = AuthenticatedUserContext.getUserId(context);
        UUID token = parseUuid(context.pathParam("token"));

        service.accept(token, userId);
        context.status(HttpStatus.NO_CONTENT);
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new NotFoundException("Este convite não existe mais.");
        }
    }
}
