package com.gymsocial.config;

import com.gymsocial.group.GroupController;
import com.gymsocial.group.GroupRepository;
import com.gymsocial.group.GroupService;
import com.gymsocial.shared.storage.ImageFileValidator;
import com.gymsocial.shared.storage.ImageStorage;
import com.gymsocial.shared.validation.RequestValidator;
import com.gymsocial.shared.id.PublicIdCodec;

import javax.sql.DataSource;

public final class GroupModule {

    private GroupModule() {
    }

    public static GroupController create(
        DataSource dataSource,
        ImageStorage imageStorage,
        PublicIdCodec publicIdCodec
    ) {
        var repository = new GroupRepository(dataSource);
        var service = new GroupService(
            repository,
            new RequestValidator(),
            new ImageFileValidator(),
            imageStorage,
            publicIdCodec
        );

        return new GroupController(service);
    }
}
