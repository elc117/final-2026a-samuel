package com.gymsocial.group;

import com.gymsocial.group.dto.CreateGroupRequest;
import com.gymsocial.group.dto.GroupResponse;
import com.gymsocial.shared.exception.ConflictException;
import com.gymsocial.shared.storage.ImageFileValidator;
import com.gymsocial.shared.storage.ImageStorage;
import com.gymsocial.shared.storage.ImageUpload;
import com.gymsocial.shared.validation.RequestValidator;
import com.gymsocial.shared.id.PublicIdCodec;

import java.util.Optional;
import java.util.UUID;

public final class GroupService {

    private static final int MAXIMUM_GROUP_IMAGE_BYTES = 1024 * 1024;

    private final GroupRepository groupRepository;
    private final RequestValidator requestValidator;
    private final ImageFileValidator imageFileValidator;
    private final ImageStorage imageStorage;
    private final PublicIdCodec publicIdCodec;

    public GroupService(
        GroupRepository groupRepository,
        RequestValidator requestValidator,
        ImageFileValidator imageFileValidator,
        ImageStorage imageStorage,
        PublicIdCodec publicIdCodec
    ) {
        this.groupRepository = groupRepository;
        this.requestValidator = requestValidator;
        this.imageFileValidator = imageFileValidator;
        this.imageStorage = imageStorage;
        this.publicIdCodec = publicIdCodec;
    }

    public Optional<GroupResponse> findCurrentGroup(long userId) {
        return groupRepository.findByUserId(userId)
            .map(result -> toResponse(
                result.group(),
                result.memberCount()
            ));
    }

    public GroupResponse create(
        long userId,
        CreateGroupRequest request,
        ImageUpload image
    ) {
        requestValidator.validate(request);

        if (groupRepository.findByUserId(userId).isPresent()) {
            throw new ConflictException("Você já participa de um grupo.");
        }

        UUID groupId = UUID.randomUUID();
        String imageKey = uploadImage(groupId, image);

        try {
            Group group = groupRepository.create(
                groupId,
                userId,
                request.name().trim(),
                imageKey
            );

            return toResponse(group, 1);
        } catch (RuntimeException exception) {
            if (imageKey != null) {
                try {
                    imageStorage.delete(imageKey);
                } catch (RuntimeException cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            }
            throw exception;
        }
    }

    private String uploadImage(UUID groupId, ImageUpload image) {
        if (image == null) {
            return null;
        }

        var validatedImage = imageFileValidator.validate(
            image,
            MAXIMUM_GROUP_IMAGE_BYTES
        );
        String objectKey = "groups/%s/cover.%s".formatted(
            groupId,
            validatedImage.extension()
        );

        imageStorage.upload(objectKey, validatedImage);
        return objectKey;
    }

    private GroupResponse toResponse(Group group, int memberCount) {
        String imageUrl = group.imageUrl() == null
            ? null
            : imageStorage.createReadUrl(group.imageUrl());

        return GroupResponse.from(
            group,
            publicIdCodec.encode(group.adminUserId()),
            imageUrl,
            memberCount
        );
    }
}
