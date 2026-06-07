package com.gymsocial.config;

import com.gymsocial.group.GroupController;
import com.gymsocial.group.GroupRepository;
import com.gymsocial.group.GroupService;
import com.gymsocial.shared.validation.RequestValidator;

import javax.sql.DataSource;

public final class GroupModule {

    private GroupModule() {
    }

    public static GroupController create(DataSource dataSource) {
        var repository = new GroupRepository(dataSource);
        var service = new GroupService(repository, new RequestValidator());

        return new GroupController(service);
    }
}
