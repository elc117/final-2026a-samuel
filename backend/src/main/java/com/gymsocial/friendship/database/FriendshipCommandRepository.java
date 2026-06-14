package com.gymsocial.friendship.database;

import com.gymsocial.friendship.domain.FriendshipPair;
import com.gymsocial.friendship.enums.AcceptResult;
import com.gymsocial.friendship.enums.FriendshipStatus;
import com.gymsocial.friendship.enums.RequestResult;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.gymsocial.friendship.FriendshipRules.MAXIMUM_CONNECTIONS;

public final class FriendshipCommandRepository
    implements com.gymsocial.friendship.port.FriendshipCommandRepository {

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

    private final DataSource dataSource;
    private final JdbcTransactionManager transactionManager;

    public FriendshipCommandRepository(
        DataSource dataSource,
        JdbcTransactionManager transactionManager
    ) {
        this.dataSource = dataSource;
        this.transactionManager = transactionManager;
    }

    @Override
    public RequestResult request(FriendshipPair pair) {
        return transactionManager.run(connection ->
            createRequest(connection, pair)
        );
    }

    private RequestResult createRequest(
        Connection connection,
        FriendshipPair pair
    ) throws SQLException {
        if (!shareGroup(connection, pair)) {
            return RequestResult.NOT_IN_SAME_GROUP;
        }

        lockUsers(connection, pair);

        if (hasReachedConnectionLimit(
            connection,
            pair,
            MAXIMUM_CONNECTIONS
        )) {
            return RequestResult.CONNECTION_LIMIT_REACHED;
        }

        Optional<FriendshipRow> existing = findPairForUpdate(connection, pair);

        if (existing.isEmpty()) {
            insert(connection, pair);
            return RequestResult.CREATED;
        }

        return handleExistingFriendship(
            connection,
            existing.get(),
            pair
        );
    }

    private RequestResult handleExistingFriendship(
            Connection connection,
            FriendshipRow friendship,
            FriendshipPair pair
    ) throws SQLException {
        if (friendship.status() == FriendshipStatus.ACCEPTED) {
            return RequestResult.ALREADY_CONNECTED;
        }

        if (friendship.status() == FriendshipStatus.PENDING) {
            return friendship.requesterUserId() == pair.firstUserId()
                    ? RequestResult.ALREADY_REQUESTED
                    : RequestResult.INCOMING_REQUEST_EXISTS;
        }

        reopen(connection, friendship.id(), pair);
        return RequestResult.CREATED;
    }

    private boolean hasReachedConnectionLimit(
            Connection connection,
            FriendshipPair pair,
            int maximumConnections
    ) throws SQLException {
        return countConnections(connection, pair.firstUserId())
                >= maximumConnections
            || countConnections(connection, pair.secondUserId())
                >= maximumConnections;
    }

    @Override
    public AcceptResult accept(UUID friendshipId, long receiverUserId) {
        return transactionManager.run(connection ->
            acceptRequest(connection, friendshipId, receiverUserId)
        );
    }

    @Override
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

    private boolean shareGroup(
        Connection connection,
        FriendshipPair pair
    ) throws SQLException {
        try (var statement = connection.prepareStatement(SHARE_GROUP)) {
            statement.setLong(1, pair.firstUserId());
            statement.setLong(2, pair.secondUserId());
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBoolean(1);
            }
        }
    }

    private Optional<FriendshipRow> findPairForUpdate(
        Connection connection,
        FriendshipPair pair
    ) throws SQLException {
        try (var statement = connection.prepareStatement(FIND_PAIR_FOR_UPDATE)) {
            statement.setLong(1, pair.firstUserId());
            statement.setLong(2, pair.secondUserId());
            statement.setLong(3, pair.firstUserId());
            statement.setLong(4, pair.secondUserId());
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
            FriendshipStatus.valueOf(resultSet.getString("status"))
        );
    }

    private void lockUsers(
        Connection connection,
        FriendshipPair pair
    ) throws SQLException {
        try (var statement = connection.prepareStatement(LOCK_USERS)) {
            statement.setLong(1, pair.firstUserId());
            statement.setLong(2, pair.secondUserId());
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
        FriendshipPair pair
    ) throws SQLException {
        Instant now = Instant.now();
        try (var statement = connection.prepareStatement(INSERT)) {
            statement.setObject(1, UUID.randomUUID());
            statement.setLong(2, pair.firstUserId());
            statement.setLong(3, pair.secondUserId());
            statement.setTimestamp(4, Timestamp.from(now));
            statement.setTimestamp(5, Timestamp.from(now));
            statement.executeUpdate();
        }
    }

    private void reopen(
        Connection connection,
        UUID friendshipId,
        FriendshipPair pair
    ) throws SQLException {
        Instant now = Instant.now();
        try (var statement = connection.prepareStatement(REOPEN)) {
            statement.setLong(1, pair.firstUserId());
            statement.setLong(2, pair.secondUserId());
            statement.setTimestamp(3, Timestamp.from(now));
            statement.setTimestamp(4, Timestamp.from(now));
            statement.setObject(5, friendshipId);
            statement.executeUpdate();
        }
    }

    private AcceptResult acceptRequest(
        Connection connection,
        UUID friendshipId,
        long receiverUserId
    ) throws SQLException {
        Optional<FriendshipRow> initialResult = findRequest(
            connection,
            friendshipId
        );
        if (
            initialResult.isEmpty() ||
            initialResult.get().receiverUserId() != receiverUserId
        ) {
            return AcceptResult.NOT_FOUND;
        }

        FriendshipRow initialFriendship = initialResult.get();
        lockUsers(connection, new FriendshipPair(
            initialFriendship.requesterUserId(),
            initialFriendship.receiverUserId()
        ));

        Optional<FriendshipRow> lockedResult = findRequestForUpdate(
            connection,
            friendshipId
        );
        if (
            lockedResult.isEmpty() ||
            lockedResult.get().receiverUserId() != receiverUserId ||
            lockedResult.get().status() != FriendshipStatus.PENDING
        ) {
            return AcceptResult.NOT_FOUND;
        }

        FriendshipRow friendship = lockedResult.get();
        if (
            hasReachedConnectionLimit(
                connection,
                new FriendshipPair(
                    friendship.requesterUserId(),
                    friendship.receiverUserId()
                ),
                MAXIMUM_CONNECTIONS
            )
        ) {
            return AcceptResult.CONNECTION_LIMIT_REACHED;
        }

        try (var statement = connection.prepareStatement(ACCEPT)) {
            statement.setTimestamp(1, Timestamp.from(Instant.now()));
            statement.setObject(2, friendshipId);
            statement.executeUpdate();
        }
        return AcceptResult.ACCEPTED;
    }
}
