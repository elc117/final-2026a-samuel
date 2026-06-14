package com.gymsocial.friendship.database;

import com.gymsocial.friendship.domain.FriendConnection;
import com.gymsocial.friendship.domain.IncomingFriendshipRequest;
import com.gymsocial.friendship.enums.Relationship;
import com.gymsocial.friendship.enums.FriendshipStatus;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.gymsocial.shared.pagination.InstantUuidCursor;

public final class FriendshipQueryRepository
    implements com.gymsocial.friendship.port.FriendshipQueryRepository {

    private static final String FIND_RELATIONSHIP = """
        SELECT requester_user_id, status
        FROM friendships
        WHERE LEAST(requester_user_id, receiver_user_id) = LEAST(?, ?)
          AND GREATEST(requester_user_id, receiver_user_id) = GREATEST(?, ?)
        """;

    private static final String FIND_INCOMING_REQUESTS = """
        SELECT friendship.id, friendship.requester_user_id,
               requester.name, requester.username,
               requester.profile_image_url, friendship.created_at
        FROM friendships friendship
        JOIN users requester ON requester.id = friendship.requester_user_id
        WHERE friendship.receiver_user_id = ?
          AND friendship.status = 'PENDING'
        ORDER BY friendship.created_at DESC
        """;

    private static final String COUNT_INCOMING_REQUESTS = """
        SELECT COUNT(*)
        FROM friendships
        WHERE receiver_user_id = ?
          AND status = 'PENDING'
        """;

    private static final String FIND_FIRST_FRIENDS_PAGE = """
        SELECT friendship.id,
               CASE
                   WHEN friendship.requester_user_id = ?
                   THEN friendship.receiver_user_id
                   ELSE friendship.requester_user_id
               END AS friend_user_id,
               friend.name, friend.username, friend.profile_image_url,
               friendship.updated_at AS connected_at
        FROM friendships friendship
        JOIN users friend ON friend.id = CASE
            WHEN friendship.requester_user_id = ?
            THEN friendship.receiver_user_id
            ELSE friendship.requester_user_id
        END
        WHERE friendship.status = 'ACCEPTED'
          AND (
              friendship.requester_user_id = ? OR
              friendship.receiver_user_id = ?
          )
        ORDER BY friendship.updated_at DESC, friendship.id DESC
        LIMIT ?
        """;

    private static final String FIND_FRIENDS_PAGE = """
        SELECT friendship.id,
               CASE
                   WHEN friendship.requester_user_id = ?
                   THEN friendship.receiver_user_id
                   ELSE friendship.requester_user_id
               END AS friend_user_id,
               friend.name, friend.username, friend.profile_image_url,
               friendship.updated_at AS connected_at
        FROM friendships friendship
        JOIN users friend ON friend.id = CASE
            WHEN friendship.requester_user_id = ?
            THEN friendship.receiver_user_id
            ELSE friendship.requester_user_id
        END
        WHERE friendship.status = 'ACCEPTED'
          AND (
              friendship.requester_user_id = ? OR
              friendship.receiver_user_id = ?
          )
          AND (
              friendship.updated_at < ? OR
              (friendship.updated_at = ? AND friendship.id < ?)
          )
        ORDER BY friendship.updated_at DESC, friendship.id DESC
        LIMIT ?
        """;

    private final DataSource dataSource;

    public FriendshipQueryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<IncomingFriendshipRequest> findIncomingRequests(
        long receiverUserId
    ) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(FIND_INCOMING_REQUESTS)
        ) {
            statement.setLong(1, receiverUserId);
            try (var resultSet = statement.executeQuery()) {
                List<IncomingFriendshipRequest> requests = new ArrayList<>();
                while (resultSet.next()) {
                    requests.add(new IncomingFriendshipRequest(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getLong("requester_user_id"),
                        resultSet.getString("name"),
                        resultSet.getString("username"),
                        resultSet.getString("profile_image_url"),
                        resultSet.getTimestamp("created_at").toInstant()
                    ));
                }
                return requests;
            }
        }
        catch (SQLException exception) {
            throw databaseFailure("list friendship requests", exception);
        }
    }

    @Override
    public int countIncomingRequests(long receiverUserId) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(
                COUNT_INCOMING_REQUESTS
            )
        ) {
            statement.setLong(1, receiverUserId);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
        catch (SQLException exception) {
            throw databaseFailure("count friendship requests", exception);
        }
    }

    @Override
    public List<FriendConnection> findFriendsPage(
        long userId,
        InstantUuidCursor cursor,
        int limit
    ) {
        String sql = cursor == null
            ? FIND_FIRST_FRIENDS_PAGE
            : FIND_FRIENDS_PAGE;

        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(sql)
        ) {
            bindFriendPage(statement, userId, cursor, limit);
            try (var resultSet = statement.executeQuery()) {
                List<FriendConnection> friends = new ArrayList<>();
                while (resultSet.next()) {
                    friends.add(new FriendConnection(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getLong("friend_user_id"),
                        resultSet.getString("name"),
                        resultSet.getString("username"),
                        resultSet.getString("profile_image_url"),
                        resultSet.getTimestamp("connected_at").toInstant()
                    ));
                }
                return friends;
            }
        }
        catch (SQLException exception) {
            throw databaseFailure("list friends", exception);
        }
    }

    @Override
    public Relationship findRelationship(long viewerUserId, long targetUserId) {
        if (viewerUserId == targetUserId) {
            return Relationship.SELF;
        }

        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(FIND_RELATIONSHIP)
        ) {
            statement.setLong(1, viewerUserId);
            statement.setLong(2, targetUserId);
            statement.setLong(3, viewerUserId);
            statement.setLong(4, targetUserId);

            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Relationship.NONE;
                }
                return mapRelationship(
                    FriendshipStatus.valueOf(resultSet.getString("status")),
                    resultSet.getLong("requester_user_id") == viewerUserId
                );
            }
        }
        catch (SQLException exception) {
            throw databaseFailure(
                "query friendship relationship",
                exception
            );
        }
    }

    private void bindFriendPage(
        java.sql.PreparedStatement statement,
        long userId,
        InstantUuidCursor cursor,
        int limit
    ) throws SQLException {
        statement.setLong(1, userId);
        statement.setLong(2, userId);
        statement.setLong(3, userId);
        statement.setLong(4, userId);

        if (cursor == null) {
            statement.setInt(5, limit);
            return;
        }

        Timestamp cursorTime = Timestamp.from(cursor.createdAt());
        statement.setTimestamp(5, cursorTime);
        statement.setTimestamp(6, cursorTime);
        statement.setObject(7, cursor.id());
        statement.setInt(8, limit);
    }

    private Relationship mapRelationship(
        FriendshipStatus status,
        boolean requestedByViewer
    ) {
        return switch (status) {
            case ACCEPTED -> Relationship.CONNECTED;
            case PENDING -> requestedByViewer
                ? Relationship.PENDING_SENT
                : Relationship.PENDING_RECEIVED;
            case REJECTED -> Relationship.NONE;
        };
    }

    private IllegalStateException databaseFailure(
        String operation,
        SQLException cause
    ) {
        return new IllegalStateException(
            "Could not " + operation,
            cause
        );
    }
}
