package com.gymsocial.group.invitation;

import com.gymsocial.group.invitation.dto.GroupInvitationResponse;
import com.gymsocial.group.invitation.dto.GroupInviteLinkResponse;
import com.gymsocial.shared.exception.ForbiddenException;
import com.gymsocial.shared.exception.NotFoundException;
import com.gymsocial.shared.storage.ImageStorage;

import java.util.UUID;

public final class GroupInvitationService {

    public static final int MAXIMUM_GROUP_MEMBERS = 10;

    private final GroupInvitationRepository repository;
    private final ImageStorage imageStorage;

    public GroupInvitationService(
        GroupInvitationRepository repository,
        ImageStorage imageStorage
    ) {
        this.repository = repository;
        this.imageStorage = imageStorage;
    }

    public GroupInviteLinkResponse findLink(
        UUID groupId,
        long userId
    ) {
        UUID token = repository.findLink(groupId, userId)
            .orElseThrow(() -> new ForbiddenException(
                "Somente integrantes do grupo podem acessar o link de convite."
            ));

        return new GroupInviteLinkResponse(token);
    }

    public GroupInvitationResponse findByToken(UUID token, long userId) {
        var invitation = repository.findByToken(token, userId)
            .orElseThrow(() -> new NotFoundException(
                "Este convite não existe mais."
            ));
        String imageUrl = invitation.groupImageKey() == null
            ? null
            : imageStorage.createReadUrl(invitation.groupImageKey());

        return new GroupInvitationResponse(
            invitation.token(),
            invitation.groupId(),
            invitation.groupName(),
            imageUrl,
            invitation.memberCount(),
            MAXIMUM_GROUP_MEMBERS,
            invitation.alreadyMember()
        );
    }

    public void accept(UUID token, long userId) {
        repository.accept(token, userId, MAXIMUM_GROUP_MEMBERS);
    }
}
