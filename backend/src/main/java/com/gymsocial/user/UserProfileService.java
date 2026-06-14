package com.gymsocial.user;

import com.gymsocial.friendship.port.FriendshipRelationshipRepository;
import com.gymsocial.friendship.enums.Relationship;
import com.gymsocial.shared.exception.NotFoundException;
import com.gymsocial.shared.exception.UnauthorizedException;
import com.gymsocial.shared.storage.ImageFileValidator;
import com.gymsocial.shared.storage.ImageStorage;
import com.gymsocial.shared.storage.ImageUpload;
import com.gymsocial.shared.validation.RequestValidator;
import com.gymsocial.shared.id.PublicIdCodec;
import com.gymsocial.user.dto.UpdateProfileRequest;
import com.gymsocial.user.dto.UserProfileResponse;

public final class UserProfileService {

    private static final int MAXIMUM_PROFILE_IMAGE_BYTES = 512 * 1024;

    private final UserRepository userRepository;
    private final RequestValidator requestValidator;
    private final ImageFileValidator imageFileValidator;
    private final ImageStorage imageStorage;
    private final PublicIdCodec publicIdCodec;
    private final FriendshipRelationshipRepository relationshipRepository;

    public UserProfileService(
        UserRepository userRepository,
        RequestValidator requestValidator,
        ImageFileValidator imageFileValidator,
        ImageStorage imageStorage,
        PublicIdCodec publicIdCodec,
        FriendshipRelationshipRepository relationshipRepository
    ) {
        this.userRepository = userRepository;
        this.requestValidator = requestValidator;
        this.imageFileValidator = imageFileValidator;
        this.imageStorage = imageStorage;
        this.publicIdCodec = publicIdCodec;
        this.relationshipRepository = relationshipRepository;
    }

    public UserProfileResponse findByUserId(long userId) {
        UserRepository.UserProfile profile = userRepository
            .findProfileById(userId)
            .orElseThrow(() -> new UnauthorizedException(
                "Usuário autenticado não encontrado."
            ));

        return toResponse(profile, Relationship.SELF);
    }

    public UserProfileResponse findVisibleProfile(
        long viewerUserId,
        String profileCode
    ) {
        long profileUserId = publicIdCodec.decode(profileCode)
            .orElseThrow(() -> new NotFoundException(
                "Perfil não encontrado."
            ));
        UserRepository.UserProfile profile = userRepository
            .findVisibleProfileById(viewerUserId, profileUserId)
            .orElseThrow(() -> new NotFoundException(
                "Perfil não encontrado."
            ));

        return toResponse(
            profile,
            relationshipRepository.findRelationship(
                viewerUserId,
                profileUserId
            )
        );
    }

    public UserProfileResponse update(
            long userId,
            UpdateProfileRequest request,
            ImageUpload image
    ) {
        requestValidator.validate(request);

        UserRepository.UserProfile currentProfile = userRepository
            .findProfileById(userId)
            .orElseThrow(() -> new UnauthorizedException(
                "Usuário autenticado não encontrado."
            ));

        String newImageKey = uploadImage(userId, image);

        User updatedUser;

        try {
            updatedUser = userRepository.updateProfile(
                userId,
                request.name().trim(),
                newImageKey == null
                    ? currentProfile.user().profileImageUrl()
                    : newImageKey
            );
        }
        catch (RuntimeException exception) {
            if (newImageKey != null) {
                deleteQuietly(newImageKey);
            }
            throw exception;
        }

        if (
            newImageKey != null &&
            currentProfile.user().profileImageUrl() != null &&
            !currentProfile.user().profileImageUrl().equals(newImageKey)
        ) {
            deleteQuietly(currentProfile.user().profileImageUrl());
        }

        return toResponse(
            new UserRepository.UserProfile(
                updatedUser,
                currentProfile.friendCount()
            ),
            Relationship.SELF
        );
    }

    private String uploadImage(long userId, ImageUpload image) {
        if (image == null) {
            return null;
        }

        var validatedImage = imageFileValidator.validate(
            image,
            MAXIMUM_PROFILE_IMAGE_BYTES
        );
        String objectKey = "users/%d/profile-%d.%s".formatted(
            userId,
            System.currentTimeMillis(),
            validatedImage.extension()
        );

        imageStorage.upload(objectKey, validatedImage);
        return objectKey;
    }

    private UserProfileResponse toResponse(
        UserRepository.UserProfile profile,
        Relationship relationship
    ) {
        String imageUrl = profile.user().profileImageUrl() == null
            ? null
            : imageStorage.createReadUrl(profile.user().profileImageUrl());

        return UserProfileResponse.from(
            profile.user(),
            publicIdCodec.encode(profile.user().id()),
            imageUrl,
            profile.friendCount(),
            relationship.name()
        );
    }

    private void deleteQuietly(String objectKey) {
        try {
            imageStorage.delete(objectKey);
        }
        catch (RuntimeException exception) {
            System.err.println(
                    "Failed to delete image during cleanup: " + objectKey
            );
        }
    }
}
