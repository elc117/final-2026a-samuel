package com.gymsocial.group;

import com.gymsocial.group.dto.CreateGroupRequest;
import com.gymsocial.group.dto.GroupResponse;
import com.gymsocial.shared.exception.ConflictException;
import com.gymsocial.shared.validation.RequestValidator;

import java.util.Optional;

public final class GroupService {

    private final GroupRepository groupRepository;
    private final RequestValidator requestValidator;

    public GroupService(
        GroupRepository groupRepository,
        RequestValidator requestValidator
    ) {
        this.groupRepository = groupRepository;
        this.requestValidator = requestValidator;
    }

    public Optional<GroupResponse> findCurrentGroup(long userId) {
        return groupRepository.findByUserId(userId)
            .map(result -> GroupResponse.from(
                result.group(),
                result.memberCount()
            ));
    }

    public GroupResponse create(long userId, CreateGroupRequest request) {
        requestValidator.validate(request);

        if (groupRepository.findByUserId(userId).isPresent()) {
            throw new ConflictException("Você já participa de um grupo.");
        }

        String imageUrl = normalizeOptional(request.imageUrl());
        Group group = groupRepository.create(
            userId,
            request.name().trim(),
            imageUrl
        );

        return GroupResponse.from(group, 1);
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
