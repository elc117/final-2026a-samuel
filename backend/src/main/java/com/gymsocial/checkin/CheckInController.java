package com.gymsocial.checkin;

import com.gymsocial.checkin.dto.CreateCheckInRequest;
import com.gymsocial.shared.exception.NotFoundException;
import com.gymsocial.shared.exception.ValidationException;
import com.gymsocial.shared.http.AuthenticatedUserContext;
import com.gymsocial.shared.storage.ImageUpload;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public final class CheckInController {

    private final CheckInService service;

    public CheckInController(CheckInService service) {
        this.service = service;
    }

    public void list(Context context) {
        long userId = AuthenticatedUserContext.getUserId(context);
        context.json(service.findCurrentGroupCheckIns(
            userId,
            context.queryParam("cursor"),
            parseLimit(context)
        ));
    }

    public void find(Context context) {
        long userId = AuthenticatedUserContext.getUserId(context);
        context.json(service.findCheckIn(userId, parseId(context)));
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

    private int parseLimit(Context context) {
        String value = context.queryParam("limit");
        if (value == null || value.isBlank()) {
            return 10;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new ValidationException(Map.of(
                "limit",
                "Informe um limite válido."
            ));
        }
    }

    private UUID parseId(Context context) {
        try {
            return UUID.fromString(context.pathParam("checkInId"));
        } catch (IllegalArgumentException exception) {
            throw new NotFoundException("Check-in não encontrado.");
        }
    }
}
