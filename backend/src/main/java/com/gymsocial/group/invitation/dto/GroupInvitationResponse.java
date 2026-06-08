package com.gymsocial.group.invitation.dto;

import java.util.UUID;

public record GroupInvitationResponse(
    UUID token,
    UUID groupId,
    String groupName,
    String groupImageUrl,
    int memberCount,
    int maximumMembers,
    boolean alreadyMember
) {
}
