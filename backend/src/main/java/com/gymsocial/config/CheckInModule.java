package com.gymsocial.config;

import com.gymsocial.checkin.CheckInController;
import com.gymsocial.checkin.CheckInRepository;
import com.gymsocial.checkin.CheckInService;
import com.gymsocial.checkin.comment.CheckInCommentController;
import com.gymsocial.checkin.comment.CheckInCommentRepository;
import com.gymsocial.shared.storage.ImageFileValidator;
import com.gymsocial.shared.storage.ImageStorage;
import com.gymsocial.shared.validation.RequestValidator;

import javax.sql.DataSource;

public final class CheckInModule {

    private CheckInModule() {
    }

    public static Components create(
        DataSource dataSource,
        ImageStorage imageStorage
    ) {
        var service = new CheckInService(
            new CheckInRepository(dataSource),
            new RequestValidator(),
            new ImageFileValidator(),
            imageStorage
        );

        return new Components(
            new CheckInController(service),
            new CheckInCommentController(
                new CheckInCommentRepository(dataSource),
                imageStorage
            )
        );
    }

    public record Components(
        CheckInController checkInController,
        CheckInCommentController commentController
    ) {
    }
}
