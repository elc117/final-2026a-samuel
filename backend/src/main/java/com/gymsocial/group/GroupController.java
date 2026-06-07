package com.gymsocial.group;

import com.gymsocial.group.dto.CreateGroupRequest;
import com.gymsocial.shared.http.AuthenticatedUserContext;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public final class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    public void current(Context context) {
        long userId = AuthenticatedUserContext.getUserId(context);

        groupService.findCurrentGroup(userId).ifPresentOrElse(
            context::json,
            () -> context.status(HttpStatus.NO_CONTENT)
        );
    }

    public void create(Context context) {
        long userId = AuthenticatedUserContext.getUserId(context);
        var response = groupService.create(
            userId,
            context.bodyAsClass(CreateGroupRequest.class)
        );

        context.status(HttpStatus.CREATED).json(response);
    }
}
