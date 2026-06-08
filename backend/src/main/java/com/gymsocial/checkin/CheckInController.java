package com.gymsocial.checkin;

import com.gymsocial.checkin.dto.CreateCheckInRequest;
import com.gymsocial.shared.http.AuthenticatedUserContext;
import com.gymsocial.shared.storage.ImageUpload;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.io.IOException;

public final class CheckInController {

    private final CheckInService service;

    public CheckInController(CheckInService service) {
        this.service = service;
    }

    public void list(Context context) {
        long userId = AuthenticatedUserContext.getUserId(context);
        context.json(service.findCurrentGroupCheckIns(userId));
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
        var request = new CreateCheckInRequest(
            context.formParam("title"),
            context.formParam("description")
        );

        context
            .status(HttpStatus.CREATED)
            .json(service.create(userId, request, image));
    }
}
