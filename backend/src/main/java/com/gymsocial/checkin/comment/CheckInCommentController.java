package com.gymsocial.checkin.comment;

import com.gymsocial.shared.exception.NotFoundException;
import com.gymsocial.shared.exception.ValidationException;
import com.gymsocial.shared.http.AuthenticatedUserContext;
import com.gymsocial.shared.storage.ImageStorage;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public final class CheckInCommentController {

    private final CheckInCommentRepository repository;
    private final ImageStorage imageStorage;

    public CheckInCommentController(
        CheckInCommentRepository repository,
        ImageStorage imageStorage
    ) {
        this.repository = repository;
        this.imageStorage = imageStorage;
    }

    public void list(Context context) {
        long userId = AuthenticatedUserContext.getUserId(context);
        context.json(repository.findByCheckIn(parseId(context), userId).stream()
            .map(this::toResponse)
            .toList());
    }

    public void create(Context context) {
        long userId = AuthenticatedUserContext.getUserId(context);
        String content = context.bodyAsClass(CreateCommentRequest.class)
            .content();

        if (content == null || content.isBlank()) {
            throw new ValidationException(Map.of(
                "content",
                "Escreva um comentário."
            ));
        }
        if (content.trim().length() > 1000) {
            throw new ValidationException(Map.of(
                "content",
                "O comentário deve ter no máximo 1000 caracteres."
            ));
        }

        context.status(HttpStatus.CREATED).json(toResponse(
            repository.create(parseId(context), userId, content.trim())
        ));
    }

    private CommentResponse toResponse(
        CheckInCommentRepository.CommentResult result
    ) {
        return new CommentResponse(
            result.id(),
            result.authorName(),
            result.authorImageKey() == null
                ? null
                : imageStorage.createReadUrl(result.authorImageKey()),
            result.content(),
            result.createdAt().toString()
        );
    }

    private UUID parseId(Context context) {
        try {
            return UUID.fromString(context.pathParam("checkInId"));
        } catch (IllegalArgumentException exception) {
            throw new NotFoundException("Check-in não encontrado.");
        }
    }

    private record CreateCommentRequest(String content) {
    }

    private record CommentResponse(
        UUID id,
        String authorName,
        String authorImageUrl,
        String content,
        String createdAt
    ) {
    }
}
