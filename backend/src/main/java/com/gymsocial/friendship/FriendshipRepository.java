package com.gymsocial.friendship;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class FriendshipRepository {

    private static final String SHARE_GROUP = """
        SELECT EXISTS (
            SELECT 1
            FROM group_members requester
            JOIN group_members receiver
              ON receiver.group_id = requester.group_id
            WHERE requester.user_id = ?
              AND receiver.user_id = ?
        )
        """;

    private static final String FIND_PAIR_FOR_UPDATE = """
        SELECT id, requester_user_id, receiver_user_id, status
        FROM friendships
        WHERE LEAST(requester_user_id, receiver_user_id) = LEAST(?, ?)
          AND GREATEST(requester_user_id, receiver_user_id) = GREATEST(?, ?)
        FOR UPDATE
        """;

    private static final String FIND_RELATION = """
        SELECT requester_user_id, receiver_user_id, status
        FROM friendships
        WHERE LEAST(requester_user_id, receiver_user_id) = LEAST(?, ?)
          AND GREATEST(requester_user_id, receiver_user_id) = GREATEST(?, ?)
        """;

    private static final String INSERT = """
        INSERT INTO friendships (
            id, requester_user_id, receiver_user_id, status,
            created_at, updated_at
        )
        VALUES (?, ?, ?, 'PENDING', ?, ?)
        """;

    private static final String REOPEN = """
        UPDATE friendships
        SET requester_user_id = ?, receiver_user_id = ?, status = 'PENDING',
            created_at = ?, updated_at = ?
        WHERE id = ?
        """;

    private static final String FIND_REQUEST_FOR_UPDATE = """
        SELECT id, requester_user_id, receiver_user_id, status
        FROM friendships
        WHERE id = ?
        FOR UPDATE
        """;

    private static final String FIND_REQUEST = """
        SELECT id, requester_user_id, receiver_user_id, status
        FROM friendships
        WHERE id = ?
        """;

    private static final String LOCK_USERS = """
        SELECT id
        FROM users
        WHERE id IN (?, ?)
        ORDER BY id
        FOR UPDATE
        """;

    private static final String COUNT_CONNECTIONS = """
        SELECT COUNT(*)
        FROM friendships
        WHERE status = 'ACCEPTED'
          AND (requester_user_id = ? OR receiver_user_id = ?)
        """;

    private static final String ACCEPT = """
        UPDATE friendships
        SET status = 'ACCEPTED', updated_at = ?
        WHERE id = ?
        """;

    private static final String REJECT = """
        UPDATE friendships
        SET status = 'REJECTED', updated_at = ?
        WHERE id = ? AND receiver_user_id = ? AND status = 'PENDING'
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

    private final DataSource dataSource;

    public FriendshipRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public RequestResult request(
        long requesterUserId,
        long receiverUserId,
        int maximumConnections
    ) {
        try (var connection = dataSource.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                if (!shareGroup(connection, requesterUserId, receiverUserId)) {
                    connection.rollback();
                    return RequestResult.NOT_IN_SAME_GROUP;
                }

                lockUsers(connection, requesterUserId, receiverUserId);
                if (
                    countConnections(connection, requesterUserId)
                        >= maximumConnections ||
                    countConnections(connection, receiverUserId)
                        >= maximumConnections
                ) {
                    connection.rollback();
                    return RequestResult.CONNECTION_LIMIT_REACHED;
                }

                Optional<FriendshipRow> existing = findPairForUpdate(
                    connection,
                    requesterUserId,
                    receiverUserId
                );
                if (existing.isPresent()) {
                    FriendshipRow friendship = existing.get();
                    if ("ACCEPTED".equals(friendship.status())) {
                        connection.rollback();
                        return RequestResult.ALREADY_CONNECTED;
                    }
                    if ("PENDING".equals(friendship.status())) {
                        connection.rollback();
                        return friendship.requesterUserId() == requesterUserId
                            ? RequestResult.ALREADY_REQUESTED
                            : RequestResult.INCOMING_REQUEST_EXISTS;
                    }
                    reopen(
                        connection,
                        friendship.id(),
                        requesterUserId,
                        receiverUserId
                    );
                } else {
                    insert(connection, requesterUserId, receiverUserId);
                }

                connection.commit();
                return RequestResult.CREATED;
            } catch (RuntimeException | SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not create friendship request",
                exception
            );
        }
    }

    public AcceptResult accept(
        UUID friendshipId,
        long receiverUserId,
        int maximumConnections
    ) {
        try (var connection = dataSource.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                Optional<FriendshipRow> initialResult = findRequest(
                    connection,
                    friendshipId
                );
                if (
                    initialResult.isEmpty() ||
                    initialResult.get().receiverUserId() != receiverUserId
                ) {
                    connection.rollback();
                    return AcceptResult.NOT_FOUND;
                }

                lockUsers(
                    connection,
                    initialResult.get().requesterUserId(),
                    initialResult.get().receiverUserId()
                );
                Optional<FriendshipRow> lockedResult = findRequestForUpdate(
                    connection,
                    friendshipId
                );
                if (
                    lockedResult.isEmpty() ||
                    lockedResult.get().receiverUserId() != receiverUserId ||
                    !"PENDING".equals(lockedResult.get().status())
                ) {
                    connection.rollback();
                    return AcceptResult.NOT_FOUND;
                }

                FriendshipRow friendship = lockedResult.get();
                if (
                    countConnections(connection, friendship.requesterUserId())
                        >= maximumConnections ||
                    countConnections(connection, friendship.receiverUserId())
                        >= maximumConnections
                ) {
                    connection.rollback();
                    return AcceptResult.CONNECTION_LIMIT_REACHED;
                }

                try (var statement = connection.prepareStatement(ACCEPT)) {
                    statement.setTimestamp(1, Timestamp.from(Instant.now()));
                    statement.setObject(2, friendshipId);
                    statement.executeUpdate();
                }
                connection.commit();
                return AcceptResult.ACCEPTED;
            } catch (RuntimeException | SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not accept friendship request",
                exception
            );
        }
    }

    public boolean reject(UUID friendshipId, long receiverUserId) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(REJECT)
        ) {
            statement.setTimestamp(1, Timestamp.from(Instant.now()));
            statement.setObject(2, friendshipId);
            statement.setLong(3, receiverUserId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not reject friendship request",
                exception
            );
        }
    }

    public List<IncomingRequest> findIncomingRequests(long receiverUserId) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(FIND_INCOMING_REQUESTS)
        ) {
            statement.setLong(1, receiverUserId);
            try (var resultSet = statement.executeQuery()) {
                List<IncomingRequest> requests = new ArrayList<>();
                while (resultSet.next()) {
                    requests.add(new IncomingRequest(
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
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not list friendship requests",
                exception
            );
        }
    }

    public Relationship findRelationship(long viewerUserId, long targetUserId) {
        if (viewerUserId == targetUserId) {
            return Relationship.SELF;
        }

        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(FIND_RELATION)
        ) {
            statement.setLong(1, viewerUserId);
            statement.setLong(2, targetUserId);
            statement.setLong(3, viewerUserId);
            statement.setLong(4, targetUserId);

            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Relationship.NONE;
                }

                return switch (resultSet.getString("status")) {
                    case "ACCEPTED" -> Relationship.CONNECTED;
                    case "PENDING" ->
                        resultSet.getLong("requester_user_id") == viewerUserId
                            ? Relationship.PENDING_SENT
                            : Relationship.PENDING_RECEIVED;
                    default -> Relationship.NONE;
                };
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not query friendship relationship",
                exception
            );
        }
    }

    private boolean shareGroup(
        Connection connection,
        long requesterUserId,
        long receiverUserId
    ) throws SQLException {
        try (var statement = connection.prepareStatement(SHARE_GROUP)) {
            statement.setLong(1, requesterUserId);
            statement.setLong(2, receiverUserId);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBoolean(1);
            }
        }
    }

    private Optional<FriendshipRow> findPairForUpdate(
        Connection connection,
        long requesterUserId,
        long receiverUserId
    ) throws SQLException {
        try (var statement = connection.prepareStatement(FIND_PAIR_FOR_UPDATE)) {
            statement.setLong(1, requesterUserId);
            statement.setLong(2, receiverUserId);
            statement.setLong(3, requesterUserId);
            statement.setLong(4, receiverUserId);
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next()
                    ? Optional.of(mapFriendship(resultSet))
                    : Optional.empty();
            }
        }
    }

    private Optional<FriendshipRow> findRequestForUpdate(
        Connection connection,
        UUID friendshipId
    ) throws SQLException {
        try (
            var statement = connection.prepareStatement(FIND_REQUEST_FOR_UPDATE)
        ) {
            statement.setObject(1, friendshipId);
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next()
                    ? Optional.of(mapFriendship(resultSet))
                    : Optional.empty();
            }
        }
    }

    private Optional<FriendshipRow> findRequest(
        Connection connection,
        UUID friendshipId
    ) throws SQLException {
        try (var statement = connection.prepareStatement(FIND_REQUEST)) {
            statement.setObject(1, friendshipId);
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next()
                    ? Optional.of(mapFriendship(resultSet))
                    : Optional.empty();
            }
        }
    }

    private FriendshipRow mapFriendship(java.sql.ResultSet resultSet)
        throws SQLException {
        return new FriendshipRow(
            resultSet.getObject("id", UUID.class),
            resultSet.getLong("requester_user_id"),
            resultSet.getLong("receiver_user_id"),
            resultSet.getString("status")
        );
    }

    private void lockUsers(
        Connection connection,
        long firstUserId,
        long secondUserId
    ) throws SQLException {
        try (var statement = connection.prepareStatement(LOCK_USERS)) {
            statement.setLong(1, firstUserId);
            statement.setLong(2, secondUserId);
            statement.executeQuery().close();
        }
    }

    private int countConnections(Connection connection, long userId)
        throws SQLException {
        try (var statement = connection.prepareStatement(COUNT_CONNECTIONS)) {
            statement.setLong(1, userId);
            statement.setLong(2, userId);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private void insert(
        Connection connection,
        long requesterUserId,
        long receiverUserId
    ) throws SQLException {
        Instant now = Instant.now();
        try (var statement = connection.prepareStatement(INSERT)) {
            statement.setObject(1, UUID.randomUUID());
            statement.setLong(2, requesterUserId);
            statement.setLong(3, receiverUserId);
            statement.setTimestamp(4, Timestamp.from(now));
            statement.setTimestamp(5, Timestamp.from(now));
            statement.executeUpdate();
        }
    }

    private void reopen(
        Connection connection,
        UUID friendshipId,
        long requesterUserId,
        long receiverUserId
    ) throws SQLException {
        Instant now = Instant.now();
        try (var statement = connection.prepareStatement(REOPEN)) {
            statement.setLong(1, requesterUserId);
            statement.setLong(2, receiverUserId);
            statement.setTimestamp(3, Timestamp.from(now));
            statement.setTimestamp(4, Timestamp.from(now));
            statement.setObject(5, friendshipId);
            statement.executeUpdate();
        }
    }

    public enum RequestResult {
        CREATED,
        ALREADY_CONNECTED,
        ALREADY_REQUESTED,
        INCOMING_REQUEST_EXISTS,
        CONNECTION_LIMIT_REACHED,
        NOT_IN_SAME_GROUP
    }

    public enum AcceptResult {
        ACCEPTED,
        NOT_FOUND,
        CONNECTION_LIMIT_REACHED
    }

    public enum Relationship {
        SELF,
        NONE,
        PENDING_SENT,
        PENDING_RECEIVED,
        CONNECTED
    }

    public record IncomingRequest(
        UUID id,
        long requesterUserId,
        String requesterName,
        String requesterUsername,
        String requesterImageKey,
        Instant createdAt
    ) {
    }

    private record FriendshipRow(
        UUID id,
        long requesterUserId,
        long receiverUserId,
        String status
    ) {
    }
}
