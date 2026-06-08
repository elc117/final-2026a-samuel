package com.gymsocial.group.invitation;

import com.gymsocial.shared.exception.ConflictException;
import com.gymsocial.shared.exception.NotFoundException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class GroupInvitationRepository {

    private static final String CREATE_OR_FIND_LINK = """
        INSERT INTO group_invite_links (
            token, group_id, created_by_user_id, created_at
        )
        SELECT ?, g.id, ?, ?
        FROM groups g
        WHERE g.id = ? AND g.admin_user_id = ?
        ON CONFLICT (group_id) DO UPDATE SET group_id = EXCLUDED.group_id
        RETURNING token
        """;

    private static final String FIND_INVITATION = """
        SELECT l.token, g.id AS group_id, g.name AS group_name,
               g.image_url, COUNT(gm.user_id) AS member_count,
               EXISTS(
                   SELECT 1
                   FROM group_members current_member
                   WHERE current_member.group_id = g.id
                     AND current_member.user_id = ?
               ) AS already_member
        FROM group_invite_links l
        JOIN groups g ON g.id = l.group_id
        LEFT JOIN group_members gm ON gm.group_id = g.id
        WHERE l.token = ?
        GROUP BY l.token, g.id
        """;

    private static final String LOCK_INVITED_GROUP = """
        SELECT g.id
        FROM group_invite_links l
        JOIN groups g ON g.id = l.group_id
        WHERE l.token = ?
        FOR UPDATE OF g
        """;

    private static final String FIND_USER_GROUP = """
        SELECT group_id
        FROM group_members
        WHERE user_id = ?
        """;

    private static final String COUNT_MEMBERS = """
        SELECT COUNT(*)
        FROM group_members
        WHERE group_id = ?
        """;

    private static final String INSERT_MEMBER = """
        INSERT INTO group_members (group_id, user_id, joined_at)
        VALUES (?, ?, ?)
        """;

    private final DataSource dataSource;

    public GroupInvitationRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<UUID> createOrFindLink(
        UUID groupId,
        long userId
    ) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(CREATE_OR_FIND_LINK)
        ) {
            statement.setObject(1, UUID.randomUUID());
            statement.setLong(2, userId);
            statement.setTimestamp(3, Timestamp.from(Instant.now()));
            statement.setObject(4, groupId);
            statement.setLong(5, userId);

            try (var resultSet = statement.executeQuery()) {
                return resultSet.next()
                    ? Optional.of(resultSet.getObject("token", UUID.class))
                    : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not create group invite link",
                exception
            );
        }
    }

    public Optional<GroupInvitation> findByToken(
        UUID token,
        long userId
    ) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(FIND_INVITATION)
        ) {
            statement.setLong(1, userId);
            statement.setObject(2, token);

            try (var resultSet = statement.executeQuery()) {
                return resultSet.next()
                    ? Optional.of(new GroupInvitation(
                        resultSet.getObject("token", UUID.class),
                        resultSet.getObject("group_id", UUID.class),
                        resultSet.getString("group_name"),
                        resultSet.getString("image_url"),
                        resultSet.getInt("member_count"),
                        resultSet.getBoolean("already_member")
                    ))
                    : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not query group invitation",
                exception
            );
        }
    }

    public void accept(UUID token, long userId, int maximumMembers) {
        try (var connection = dataSource.getConnection()) {
            acceptInTransaction(connection, token, userId, maximumMembers);
        } catch (SQLException exception) {
            if ("23505".equals(exception.getSQLState())) {
                throw new ConflictException(
                    "Você já participa de outro grupo."
                );
            }
            throw new IllegalStateException(
                "Could not accept group invitation",
                exception
            );
        }
    }

    private void acceptInTransaction(
        Connection connection,
        UUID token,
        long userId,
        int maximumMembers
    ) throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            UUID groupId = lockInvitedGroup(connection, token);
            Optional<UUID> currentGroupId = findUserGroup(connection, userId);

            if (currentGroupId.isPresent()) {
                if (currentGroupId.get().equals(groupId)) {
                    connection.commit();
                    return;
                }

                throw new ConflictException(
                    "Você já participa de outro grupo."
                );
            }

            if (countMembers(connection, groupId) >= maximumMembers) {
                throw new ConflictException(
                    "Este grupo já atingiu o limite de participantes."
                );
            }

            insertMember(connection, groupId, userId);
            connection.commit();
        } catch (RuntimeException | SQLException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private UUID lockInvitedGroup(Connection connection, UUID token)
        throws SQLException {
        try (var statement = connection.prepareStatement(LOCK_INVITED_GROUP)) {
            statement.setObject(1, token);

            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new NotFoundException(
                        "Este convite não existe mais."
                    );
                }

                return resultSet.getObject("id", UUID.class);
            }
        }
    }

    private Optional<UUID> findUserGroup(
        Connection connection,
        long userId
    ) throws SQLException {
        try (var statement = connection.prepareStatement(FIND_USER_GROUP)) {
            statement.setLong(1, userId);

            try (var resultSet = statement.executeQuery()) {
                return resultSet.next()
                    ? Optional.of(resultSet.getObject("group_id", UUID.class))
                    : Optional.empty();
            }
        }
    }

    private int countMembers(Connection connection, UUID groupId)
        throws SQLException {
        try (var statement = connection.prepareStatement(COUNT_MEMBERS)) {
            statement.setObject(1, groupId);

            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private void insertMember(
        Connection connection,
        UUID groupId,
        long userId
    ) throws SQLException {
        try (var statement = connection.prepareStatement(INSERT_MEMBER)) {
            statement.setObject(1, groupId);
            statement.setLong(2, userId);
            statement.setTimestamp(3, Timestamp.from(Instant.now()));
            statement.executeUpdate();
        }
    }

    public record GroupInvitation(
        UUID token,
        UUID groupId,
        String groupName,
        String groupImageKey,
        int memberCount,
        boolean alreadyMember
    ) {
    }
}
