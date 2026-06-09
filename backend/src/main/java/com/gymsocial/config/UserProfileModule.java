package com.gymsocial.config;

import com.gymsocial.shared.storage.ImageFileValidator;
import com.gymsocial.shared.storage.ImageStorage;
import com.gymsocial.shared.validation.RequestValidator;
import com.gymsocial.shared.id.PublicIdCodec;
import com.gymsocial.user.UserProfileController;
import com.gymsocial.user.UserProfileService;
import com.gymsocial.user.UserRepository;

import javax.sql.DataSource;

public final class UserProfileModule {

    private UserProfileModule() {
    }

    public static UserProfileController create(
        DataSource dataSource,
        ImageStorage imageStorage,
        PublicIdCodec publicIdCodec
    ) {
        var service = new UserProfileService(
            new UserRepository(dataSource),
            new RequestValidator(),
            new ImageFileValidator(),
            imageStorage,
            publicIdCodec
        );

        return new UserProfileController(service);
    }
}
