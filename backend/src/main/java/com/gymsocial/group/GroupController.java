package com.gymsocial.group;

import com.gymsocial.group.dto.CreateGroupRequest;
import com.gymsocial.shared.http.AuthenticatedUserContext;
import com.gymsocial.shared.storage.ImageUpload;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.io.IOException;

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

    public void members(Context context) {
        context.json(groupService.findCurrentGroupMembers(
            AuthenticatedUserContext.getUserId(context)
        ));
    }

    public void create(Context context) throws IOException {
        long userId = AuthenticatedUserContext.getUserId(context);
        var uploadedFile = context.uploadedFile("image");
        ImageUpload image = uploadedFile == null
            ? null
            : new ImageUpload(
                uploadedFile.filename(),
                uploadedFile.contentType(),
                uploadedFile.content().readAllBytes()
            );
        var response = groupService.create(
            userId,
            new CreateGroupRequest(context.formParam("name")),
            image
        );

        context.status(HttpStatus.CREATED).json(response);
    }
}
