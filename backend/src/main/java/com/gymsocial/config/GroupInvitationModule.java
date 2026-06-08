package com.gymsocial.config;

import com.gymsocial.group.invitation.GroupInvitationController;
import com.gymsocial.group.invitation.GroupInvitationRepository;
import com.gymsocial.group.invitation.GroupInvitationService;
import com.gymsocial.shared.storage.ImageStorage;

import javax.sql.DataSource;

public final class GroupInvitationModule {

    private GroupInvitationModule() {
    }

    public static GroupInvitationController create(
        DataSource dataSource,
        ImageStorage imageStorage
    ) {
        var service = new GroupInvitationService(
            new GroupInvitationRepository(dataSource),
            imageStorage
        );

        return new GroupInvitationController(service);
    }
}
