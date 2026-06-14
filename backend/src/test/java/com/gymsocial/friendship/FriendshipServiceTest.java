package com.gymsocial.friendship;

import com.gymsocial.friendship.domain.FriendConnection;
import com.gymsocial.friendship.domain.FriendshipPair;
import com.gymsocial.friendship.domain.IncomingFriendshipRequest;
import com.gymsocial.friendship.enums.AcceptResult;
import com.gymsocial.friendship.enums.Relationship;
import com.gymsocial.friendship.enums.RequestResult;
import com.gymsocial.friendship.port.FriendshipCommandRepository;
import com.gymsocial.friendship.port.FriendshipQueryRepository;
import com.gymsocial.shared.exception.ForbiddenException;
import com.gymsocial.shared.exception.ValidationException;
import com.gymsocial.shared.id.PublicIdCodec;
import com.gymsocial.shared.pagination.InstantUuidCursor;
import com.gymsocial.shared.storage.ImageStorage;
import com.gymsocial.shared.storage.ValidatedImage;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FriendshipServiceTest {

    private static final long REQUESTER_ID = 10L;
    private static final long RECEIVER_ID = 20L;

    private final PublicIdCodec publicIdCodec =
        new PublicIdCodec("friendship-test-salt");
    private final StubFriendshipRepository repository =
        new StubFriendshipRepository();
    private final FriendshipService service = new FriendshipService(
        repository,
        repository,
        new FriendshipResponseMapper(
            publicIdCodec,
            new StubImageStorage()
        ),
        publicIdCodec
    );

    @Test
    void shouldSendRequestUsingDecodedPublicId() {
        service.request(REQUESTER_ID, publicIdCodec.encode(RECEIVER_ID));

        assertEquals(REQUESTER_ID, repository.pair.firstUserId());
        assertEquals(RECEIVER_ID, repository.pair.secondUserId());
    }

    @Test
    void shouldRejectRequestOutsideTheGroup() {
        repository.requestResult = RequestResult.NOT_IN_SAME_GROUP;

        assertThrows(
            ForbiddenException.class,
            () -> service.request(
                REQUESTER_ID,
                publicIdCodec.encode(RECEIVER_ID)
            )
        );
    }

    @Test
    void shouldCreateNextCursorWhenAnotherPageExists() {
        repository.friends = List.of(
            friend("2026-06-14T12:00:00Z"),
            friend("2026-06-13T12:00:00Z")
        );

        var page = service.findFriends(REQUESTER_ID, null, 1);

        assertEquals(1, page.items().size());
        assertTrue(page.hasMore());
        assertNotNull(page.nextCursor());
        assertEquals(2, repository.pageLimit);
    }

    @Test
    void shouldReturnLastPageWithoutCursor() {
        repository.friends = List.of(friend("2026-06-14T12:00:00Z"));

        var page = service.findFriends(REQUESTER_ID, null, 20);

        assertEquals(1, page.items().size());
        assertFalse(page.hasMore());
        assertNull(page.nextCursor());
    }

    @Test
    void shouldRejectPageSizeAboveLimit() {
        assertThrows(
            ValidationException.class,
            () -> service.findFriends(REQUESTER_ID, null, 51)
        );
    }

    private FriendConnection friend(String connectedAt) {
        return new FriendConnection(
            UUID.randomUUID(),
            RECEIVER_ID,
            "Friend",
            "friend",
            "profile.webp",
            Instant.parse(connectedAt)
        );
    }

    private static final class StubFriendshipRepository
        implements FriendshipCommandRepository, FriendshipQueryRepository {

        private RequestResult requestResult = RequestResult.CREATED;
        private FriendshipPair pair;
        private List<FriendConnection> friends = List.of();
        private int pageLimit;

        @Override
        public RequestResult request(FriendshipPair friendshipPair) {
            pair = friendshipPair;
            return requestResult;
        }

        @Override
        public AcceptResult accept(UUID friendshipId, long receiverUserId) {
            return AcceptResult.ACCEPTED;
        }

        @Override
        public boolean reject(UUID friendshipId, long receiverUserId) {
            return true;
        }

        @Override
        public List<IncomingFriendshipRequest> findIncomingRequests(
            long receiverUserId
        ) {
            return List.of();
        }

        @Override
        public int countIncomingRequests(long receiverUserId) {
            return 0;
        }

        @Override
        public List<FriendConnection> findFriendsPage(
            long userId,
            InstantUuidCursor cursor,
            int limit
        ) {
            pageLimit = limit;
            return friends;
        }

        @Override
        public Relationship findRelationship(
            long viewerUserId,
            long targetUserId
        ) {
            return Relationship.NONE;
        }
    }

    private static final class StubImageStorage implements ImageStorage {

        @Override
        public void upload(String objectKey, ValidatedImage image) {
        }

        @Override
        public String createReadUrl(String objectKey) {
            return "https://images.test/" + objectKey;
        }

        @Override
        public void delete(String objectKey) {
        }
    }
}
