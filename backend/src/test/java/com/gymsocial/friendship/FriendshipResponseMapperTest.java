package com.gymsocial.friendship;

import com.gymsocial.friendship.domain.FriendConnection;
import com.gymsocial.friendship.domain.IncomingFriendshipRequest;
import com.gymsocial.shared.id.PublicIdCodec;
import com.gymsocial.shared.storage.ImageStorage;
import com.gymsocial.shared.storage.ValidatedImage;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class FriendshipResponseMapperTest {

    private static final Instant DATE = Instant.parse("2026-06-15T12:30:00Z");

    private final FriendshipResponseMapper mapper = new FriendshipResponseMapper(
        new PublicIdCodec("test-salt"),
        new NoOpImageStorage()
    );

    @Test
    void shouldMapConnectionDateAsIsoText() {
        var friend = new FriendConnection(
            UUID.randomUUID(),
            2L,
            "Friend",
            "friend",
            null,
            DATE
        );

        assertEquals(DATE.toString(), mapper.toFriendResponse(friend).connectedAt());
    }

    @Test
    void shouldMapRequestDateAsIsoText() {
        var request = new IncomingFriendshipRequest(
            UUID.randomUUID(),
            2L,
            "Friend",
            "friend",
            null,
            DATE
        );

        assertEquals(DATE.toString(), mapper.toRequestResponse(request).createdAt());
    }

    private static final class NoOpImageStorage implements ImageStorage {

        @Override
        public void upload(String objectKey, ValidatedImage image) {
        }

        @Override
        public String createReadUrl(String objectKey) {
            return objectKey;
        }

        @Override
        public void delete(String objectKey) {
        }
    }
}
