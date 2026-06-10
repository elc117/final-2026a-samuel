package com.gymsocial.friendship;

import com.gymsocial.friendship.dto.FriendshipRequestResponse;
import com.gymsocial.friendship.dto.FriendResponse;
import com.gymsocial.shared.exception.ConflictException;
import com.gymsocial.shared.exception.ForbiddenException;
import com.gymsocial.shared.exception.NotFoundException;
import com.gymsocial.shared.exception.ValidationException;
import com.gymsocial.shared.id.PublicIdCodec;
import com.gymsocial.shared.pagination.CursorPage;
import com.gymsocial.shared.pagination.InstantUuidCursorCodec;
import com.gymsocial.shared.storage.ImageStorage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class FriendshipService {

    private static final int MAXIMUM_CONNECTIONS = 500;
    private static final int MAXIMUM_PAGE_SIZE = 50;

    private final FriendshipRepository repository;
    private final PublicIdCodec publicIdCodec;
    private final ImageStorage imageStorage;
    private final InstantUuidCursorCodec cursorCodec;

    public FriendshipService(
        FriendshipRepository repository,
        PublicIdCodec publicIdCodec,
        ImageStorage imageStorage
    ) {
        this.repository = repository;
        this.publicIdCodec = publicIdCodec;
        this.imageStorage = imageStorage;
        this.cursorCodec = new InstantUuidCursorCodec();
    }

    public void request(long requesterUserId, String receiverCode) {
        long receiverUserId = publicIdCodec.decode(receiverCode)
            .orElseThrow(() -> new NotFoundException(
                "Perfil não encontrado."
            ));
        if (requesterUserId == receiverUserId) {
            throw new ConflictException(
                "Você não pode se conectar consigo mesmo."
            );
        }

        switch (repository.request(
            requesterUserId,
            receiverUserId,
            MAXIMUM_CONNECTIONS
        )) {
            case CREATED -> {
            }
            case ALREADY_CONNECTED -> throw new ConflictException(
                "Vocês já estão conectados."
            );
            case ALREADY_REQUESTED -> throw new ConflictException(
                "A solicitação já foi enviada."
            );
            case INCOMING_REQUEST_EXISTS -> throw new ConflictException(
                "Essa pessoa já enviou uma solicitação para você."
            );
            case CONNECTION_LIMIT_REACHED -> throw new ConflictException(
                "Uma das pessoas já atingiu o limite de 500 conexões."
            );
            case NOT_IN_SAME_GROUP -> throw new ForbiddenException(
                "Você só pode se conectar com pessoas do seu grupo."
            );
        }
    }

    public List<FriendshipRequestResponse> findIncoming(long userId) {
        return repository.findIncomingRequests(userId).stream()
            .map(request -> new FriendshipRequestResponse(
                request.id(),
                publicIdCodec.encode(request.requesterUserId()),
                request.requesterName(),
                request.requesterUsername(),
                request.requesterImageKey() == null
                    ? null
                    : imageStorage.createReadUrl(
                        request.requesterImageKey()
                    ),
                request.createdAt()
            ))
            .toList();
    }

    public int countIncoming(long userId) {
        return repository.countIncomingRequests(userId);
    }

    public CursorPage<FriendResponse> findFriends(
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

        var results = repository.findFriendsPage(
            userId,
            cursorCodec.decode(encodedCursor),
            pageSize + 1
        );
        boolean hasMore = results.size() > pageSize;
        var pageResults = hasMore
            ? results.subList(0, pageSize)
            : results;
        List<FriendResponse> items = pageResults.stream()
            .map(friend -> new FriendResponse(
                publicIdCodec.encode(friend.userId()),
                friend.name(),
                friend.username(),
                friend.profileImageKey() == null
                    ? null
                    : imageStorage.createReadUrl(friend.profileImageKey()),
                friend.connectedAt()
            ))
            .toList();
        String nextCursor = hasMore && !pageResults.isEmpty()
            ? cursorCodec.encode(
                pageResults.getLast().connectedAt(),
                pageResults.getLast().friendshipId()
            )
            : null;

        return new CursorPage<>(items, nextCursor, hasMore);
    }

    public void accept(long receiverUserId, UUID friendshipId) {
        switch (repository.accept(
            friendshipId,
            receiverUserId,
            MAXIMUM_CONNECTIONS
        )) {
            case ACCEPTED -> {
            }
            case NOT_FOUND -> throw new NotFoundException(
                "Solicitação de conexão não encontrada."
            );
            case CONNECTION_LIMIT_REACHED -> throw new ConflictException(
                "Uma das pessoas já atingiu o limite de 500 conexões."
            );
        }
    }

    public void reject(long receiverUserId, UUID friendshipId) {
        if (!repository.reject(friendshipId, receiverUserId)) {
            throw new NotFoundException(
                "Solicitação de conexão não encontrada."
            );
        }
    }
}
