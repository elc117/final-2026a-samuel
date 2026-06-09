package com.gymsocial.user;

import com.gymsocial.shared.http.AuthenticatedUserContext;
import com.gymsocial.shared.storage.ImageUpload;
import com.gymsocial.user.dto.UpdateProfileRequest;
import io.javalin.http.Context;

import java.io.IOException;

public final class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    public void current(Context context) {
        long userId = AuthenticatedUserContext.getUserId(context);
        context.json(userProfileService.findByUserId(userId));
    }

    public void find(Context context) {
        long viewerUserId = AuthenticatedUserContext.getUserId(context);
        context.json(userProfileService.findVisibleProfile(
            viewerUserId,
            context.pathParam("userCode")
        ));
    }

    public void update(Context context) throws IOException {
        long userId = AuthenticatedUserContext.getUserId(context);
        var uploadedFile = context.uploadedFile("image");
        ImageUpload image = uploadedFile == null
            ? null
            : new ImageUpload(
                uploadedFile.filename(),
                uploadedFile.contentType(),
                uploadedFile.content().readAllBytes()
            );

        context.json(userProfileService.update(
            userId,
            new UpdateProfileRequest(context.formParam("name")),
            image
        ));
    }
}
