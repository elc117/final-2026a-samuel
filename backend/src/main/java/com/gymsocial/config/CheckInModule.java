package com.gymsocial.config;

import com.gymsocial.checkin.CheckInController;
import com.gymsocial.checkin.CheckInRepository;
import com.gymsocial.checkin.CheckInService;
import com.gymsocial.shared.storage.ImageFileValidator;
import com.gymsocial.shared.storage.ImageStorage;
import com.gymsocial.shared.validation.RequestValidator;

import javax.sql.DataSource;

public final class CheckInModule {

    private CheckInModule() {
    }

    public static CheckInController create(
        DataSource dataSource,
        ImageStorage imageStorage
    ) {
        var service = new CheckInService(
            new CheckInRepository(dataSource),
            new RequestValidator(),
            new ImageFileValidator(),
            imageStorage
        );

        return new CheckInController(service);
    }
}
