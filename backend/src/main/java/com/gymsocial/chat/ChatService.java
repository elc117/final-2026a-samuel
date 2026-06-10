package com.gymsocial.chat;

import com.gymsocial.chat.dto.ChatMessageResponse;
import com.gymsocial.chat.dto.ChatSessionResponse;
import com.gymsocial.chat.dto.SendChatMessageRequest;
import com.gymsocial.shared.exception.ForbiddenException;
import com.gymsocial.shared.exception.ValidationException;
import com.gymsocial.shared.id.PublicIdCodec;
import com.gymsocial.shared.pagination.CursorPage;
import com.gymsocial.shared.pagination.InstantUuidCursorCodec;
import com.gymsocial.shared.storage.ImageStorage;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ChatService {

    private static final int MAXIMUM_CONTENT_LENGTH = 2000;
    private static final int MAXIMUM_PAGE_SIZE = 50;

    private final ChatRepository repository;
    private final ImageStorage imageStorage;
    private final PublicIdCodec publicIdCodec;
    private final InstantUuidCursorCodec cursorCodec;

    public ChatService(
        ChatRepository repository,
        ImageStorage imageStorage,
        PublicIdCodec publicIdCodec
    ) {
        this.repository = repository;
        this.imageStorage = imageStorage;
        this.publicIdCodec = publicIdCodec;
        this.cursorCodec = new InstantUuidCursorCodec();
    }

    public ChatSessionResponse findSession(long userId) {
        var session = repository.findSession(userId)
            .orElseThrow(this::groupRequired);

        return new ChatSessionResponse(
            session.groupId(),
            publicIdCodec.encode(session.userId()),
            session.userName(),
            createImageUrl(session.userImageUrl())
        );
    }

    public CursorPage<ChatMessageResponse> findMessages(
        long userId,
        String encodedCursor,
        int pageSize
    ) {
        if (pageSize < 1 || pageSize > MAXIMUM_PAGE_SIZE) {
            throw new ValidationException(Map.of(
                "limit",
                "O limite deve estar entre 1 e 50."
            ));
        }

        var cursor = cursorCodec.decode(encodedCursor);
        List<ChatRepository.ChatMessage> results = repository.findPage(
            userId,
            cursor,
            pageSize + 1
        );
        boolean hasMore = results.size() > pageSize;
        List<ChatRepository.ChatMessage> pageResults = hasMore
            ? results.subList(0, pageSize)
            : results;
        List<ChatMessageResponse> items = pageResults.stream()
            .map(this::toResponse)
            .toList();
        String nextCursor = hasMore && !pageResults.isEmpty()
            ? cursorCodec.encode(
                pageResults.getLast().createdAt(),
                pageResults.getLast().id()
            )
            : null;

        return new CursorPage<>(items, nextCursor, hasMore);
    }

    public ChatMessageResponse send(
        long userId,
        SendChatMessageRequest request
    ) {
        String content = normalizeContent(request == null
            ? null
            : request.content());
        UUID messageId = UUID.randomUUID();

        if (!repository.createForGroupMember(
            messageId,
            userId,
            content,
            Instant.now()
        )) {
            throw groupRequired();
        }

        return repository.findByIdForGroupMember(messageId, userId)
            .map(this::toResponse)
            .orElseThrow(() -> new IllegalStateException(
                "Could not load created chat message"
            ));
    }

    private String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            throw new ValidationException(Map.of(
                "content",
                "Escreva uma mensagem."
            ));
        }

        String normalized = content.trim();
        if (normalized.length() > MAXIMUM_CONTENT_LENGTH) {
            throw new ValidationException(Map.of(
                "content",
                "A mensagem deve ter no máximo 2000 caracteres."
            ));
        }
        return normalized;
    }

    private ChatMessageResponse toResponse(
        ChatRepository.ChatMessage message
    ) {
        return new ChatMessageResponse(
            message.id(),
            message.groupId(),
            publicIdCodec.encode(message.authorUserId()),
            message.authorName(),
            createImageUrl(message.authorImageUrl()),
            message.content(),
            message.createdAt()
        );
    }

    private String createImageUrl(String imageKey) {
        return imageKey == null ? null : imageStorage.createReadUrl(imageKey);
    }

    private ForbiddenException groupRequired() {
        return new ForbiddenException(
            "Você precisa participar de um grupo para acessar o chat."
        );
    }
}
