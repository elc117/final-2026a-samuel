package com.gymsocial.friendship;

import com.gymsocial.friendship.domain.FriendshipPair;
import com.gymsocial.friendship.dto.FriendResponse;
import com.gymsocial.friendship.dto.FriendshipRequestResponse;
import com.gymsocial.friendship.enums.RequestResult;
import com.gymsocial.friendship.port.FriendshipCommandRepository;
import com.gymsocial.friendship.port.FriendshipQueryRepository;
import com.gymsocial.shared.exception.ConflictException;
import com.gymsocial.shared.exception.ForbiddenException;
import com.gymsocial.shared.exception.NotFoundException;
import com.gymsocial.shared.exception.ValidationException;
import com.gymsocial.shared.id.PublicIdCodec;
import com.gymsocial.shared.pagination.CursorPage;
import com.gymsocial.shared.pagination.InstantUuidCursorCodec;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class FriendshipService {

    private static final int MAXIMUM_PAGE_SIZE = 50;

    private final FriendshipCommandRepository commandRepository;
    private final FriendshipQueryRepository queryRepository;
    private final FriendshipResponseMapper responseMapper;
    private final PublicIdCodec publicIdCodec;
    private final InstantUuidCursorCodec cursorCodec;

    public FriendshipService(
        FriendshipCommandRepository commandRepository,
        FriendshipQueryRepository queryRepository,
        FriendshipResponseMapper responseMapper,
        PublicIdCodec publicIdCodec
    ) {
        this.commandRepository = commandRepository;
        this.queryRepository = queryRepository;
        this.responseMapper = responseMapper;
        this.publicIdCodec = publicIdCodec;
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

        RequestResult result = commandRepository.request(
            new FriendshipPair(requesterUserId, receiverUserId)
        );
        ensureRequestSucceeded(result);
    }

    public void accept(long receiverUserId, UUID friendshipId) {
        switch (commandRepository.accept(friendshipId, receiverUserId)) {
            case ACCEPTED -> {
            }
            case NOT_FOUND -> throw requestNotFound();
            case CONNECTION_LIMIT_REACHED -> throw connectionLimitReached();
        }
    }

    public void reject(long receiverUserId, UUID friendshipId) {
        if (!commandRepository.reject(friendshipId, receiverUserId)) {
            throw requestNotFound();
        }
    }

    public List<FriendshipRequestResponse> findIncoming(long userId) {
        return queryRepository.findIncomingRequests(userId).stream()
            .map(responseMapper::toRequestResponse)
            .toList();
    }

    public int countIncoming(long userId) {
        return queryRepository.countIncomingRequests(userId);
    }

    public CursorPage<FriendResponse> findFriends(long userId, String encodedCursor, int pageSize) {
        validatePageSize(pageSize);

        var results = queryRepository.findFriendsPage(
            userId,
            cursorCodec.decode(encodedCursor),
            pageSize + 1
        );
        boolean hasMore = results.size() > pageSize;
        var pageResults = hasMore
            ? results.subList(0, pageSize)
            : results;
        var items = pageResults.stream()
            .map(responseMapper::toFriendResponse)
            .toList();
        String nextCursor = hasMore && !pageResults.isEmpty()
            ? cursorCodec.encode(
                pageResults.getLast().connectedAt(),
                pageResults.getLast().friendshipId()
            )
            : null;

        return new CursorPage<>(items, nextCursor, hasMore);
    }

    private void ensureRequestSucceeded(RequestResult result) {
        switch (result) {
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
            case CONNECTION_LIMIT_REACHED -> throw connectionLimitReached();
            case NOT_IN_SAME_GROUP -> throw new ForbiddenException(
                "Você só pode se conectar com pessoas do seu grupo."
            );
        }
    }

    private void validatePageSize(int pageSize) {
        if (pageSize < 1 || pageSize > MAXIMUM_PAGE_SIZE) {
            throw new ValidationException(Map.of(
                "limit",
                "O limite deve estar entre 1 e 50."
            ));
        }
    }

    private ConflictException connectionLimitReached() {
        return new ConflictException(
            "Uma das pessoas já atingiu o limite de 500 conexões."
        );
    }

    private NotFoundException requestNotFound() {
        return new NotFoundException(
            "Solicitação de conexão não encontrada."
        );
    }
}
