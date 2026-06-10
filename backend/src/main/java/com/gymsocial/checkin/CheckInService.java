package com.gymsocial.checkin;

import com.gymsocial.checkin.dto.CheckInResponse;
import com.gymsocial.checkin.dto.CreateCheckInRequest;
import com.gymsocial.shared.exception.ForbiddenException;
import com.gymsocial.shared.exception.NotFoundException;
import com.gymsocial.shared.exception.ValidationException;
import com.gymsocial.shared.storage.ImageFileValidator;
import com.gymsocial.shared.storage.ImageStorage;
import com.gymsocial.shared.storage.ImageUpload;
import com.gymsocial.shared.validation.RequestValidator;
import com.gymsocial.shared.id.PublicIdCodec;
import com.gymsocial.shared.pagination.CursorPage;
import com.gymsocial.shared.pagination.InstantUuidCursorCodec;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CheckInService {

    private static final int MAXIMUM_IMAGE_BYTES = 2 * 1024 * 1024;
    private static final int MAXIMUM_PAGE_SIZE = 30;

    private final CheckInRepository repository;
    private final RequestValidator requestValidator;
    private final ImageFileValidator imageFileValidator;
    private final ImageStorage imageStorage;
    private final PublicIdCodec publicIdCodec;
    private final InstantUuidCursorCodec cursorCodec;

    public CheckInService(
        CheckInRepository repository,
        RequestValidator requestValidator,
        ImageFileValidator imageFileValidator,
        ImageStorage imageStorage,
        PublicIdCodec publicIdCodec
    ) {
        this.repository = repository;
        this.requestValidator = requestValidator;
        this.imageFileValidator = imageFileValidator;
        this.imageStorage = imageStorage;
        this.publicIdCodec = publicIdCodec;
        this.cursorCodec = new InstantUuidCursorCodec();
    }

    public CheckInResponse create(
        long userId,
        CreateCheckInRequest request,
        ImageUpload image
    ) {
        requestValidator.validate(request);

        if (image == null) {
            throw new ValidationException(Map.of(
                "image",
                "Selecione uma imagem para o check-in."
            ));
        }

        UUID groupId = repository.findGroupIdByUserId(userId)
            .orElseThrow(() -> new ForbiddenException(
                "Você precisa participar de um grupo para fazer check-in."
            ));
        UUID checkInId = UUID.randomUUID();
        String imageKey = uploadImage(checkInId, image);
        var checkIn = new CheckIn(
            checkInId,
            groupId,
            userId,
            request.title().trim(),
            normalizeDescription(request.description()),
            imageKey,
            Instant.now()
        );

        try {
            repository.create(checkIn);
        } catch (RuntimeException exception) {
            deleteQuietly(imageKey, exception);
            throw exception;
        }

        var created = repository.findById(checkIn.id())
            .orElseThrow(() -> new IllegalStateException(
                "Could not load created check-in"
            ));

        return toResponse(
            created.checkIn(),
            created.authorName(),
            created.authorImageUrl()
        );
    }

    public CursorPage<CheckInResponse> findCurrentGroupCheckIns(
        long userId,
        String encodedCursor,
        int pageSize
    ) {
        if (pageSize < 1 || pageSize > MAXIMUM_PAGE_SIZE) {
            throw new ValidationException(Map.of(
                "limit",
                "O limite deve estar entre 1 e 30."
            ));
        }

        var cursor = cursorCodec.decode(encodedCursor);
        List<CheckInRepository.CheckInWithAuthor> results =
            repository.findPageByGroupMember(
                userId,
                cursor,
                pageSize + 1
            );
        boolean hasMore = results.size() > pageSize;
        List<CheckInRepository.CheckInWithAuthor> pageResults = hasMore
            ? results.subList(0, pageSize)
            : results;
        List<CheckInResponse> items = pageResults.stream()
            .map(result -> toResponse(
                result.checkIn(),
                result.authorName(),
                result.authorImageUrl()
            ))
            .toList();
        String nextCursor = hasMore && !pageResults.isEmpty()
            ? cursorCodec.encode(
                pageResults.getLast().checkIn().createdAt(),
                pageResults.getLast().checkIn().id()
            )
            : null;

        return new CursorPage<>(items, nextCursor, hasMore);
    }

    public CheckInResponse findCheckIn(long userId, UUID checkInId) {
        var result = repository.findByIdForMember(checkInId, userId)
            .orElseThrow(() -> new NotFoundException(
                "Check-in não encontrado."
            ));

        return toResponse(
            result.checkIn(),
            result.authorName(),
            result.authorImageUrl()
        );
    }

    private String uploadImage(UUID checkInId, ImageUpload image) {
        var validatedImage = imageFileValidator.validate(
            image,
            MAXIMUM_IMAGE_BYTES
        );
        String objectKey = "check-ins/%s/image.%s".formatted(
            checkInId,
            validatedImage.extension()
        );

        imageStorage.upload(objectKey, validatedImage);
        return objectKey;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }

    private CheckInResponse toResponse(
        CheckIn checkIn,
        String authorName,
        String authorImageKey
    ) {
        String imageUrl = checkIn.imageUrl() == null
            ? null
            : imageStorage.createReadUrl(checkIn.imageUrl());
        String authorImageUrl = authorImageKey == null
            ? null
            : imageStorage.createReadUrl(authorImageKey);

        return CheckInResponse.from(
            checkIn,
            publicIdCodec.encode(checkIn.authorUserId()),
            authorName,
            authorImageUrl,
            imageUrl
        );
    }

    private void deleteQuietly(String imageKey, RuntimeException exception) {
        if (imageKey == null) {
            return;
        }

        try {
            imageStorage.delete(imageKey);
        } catch (RuntimeException cleanupException) {
            exception.addSuppressed(cleanupException);
        }
    }
}
