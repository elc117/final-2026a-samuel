package com.gymsocial.group;

import com.gymsocial.shared.exception.ConflictException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class GroupRepository {

    private static final String FIND_BY_USER_ID = """
        SELECT g.id, g.admin_user_id, g.name, g.image_url,
               g.created_at, g.updated_at, COUNT(gm_all.user_id) AS member_count
        FROM groups g
        JOIN group_members gm_user ON gm_user.group_id = g.id
        JOIN group_members gm_all ON gm_all.group_id = g.id
        WHERE gm_user.user_id = ?
        GROUP BY g.id
        """;

    private static final String INSERT_GROUP = """
        INSERT INTO groups (
            id, admin_user_id, name, image_url, created_at, updated_at
        )
        VALUES (?, ?, ?, ?, ?, ?)
        """;

    private static final String INSERT_MEMBER = """
        INSERT INTO group_members (group_id, user_id, joined_at)
        VALUES (?, ?, ?)
        """;

    private final DataSource dataSource;

    public GroupRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<GroupWithMemberCount> findByUserId(long userId) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(FIND_BY_USER_ID)
        ) {
            statement.setLong(1, userId);

            try (var resultSet = statement.executeQuery()) {
                return resultSet.next()
                    ? Optional.of(mapGroup(resultSet))
                    : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not query group", exception);
        }
    }

    public Group create(
        UUID groupId,
        long adminUserId,
        String name,
        String imageUrl
    ) {
        Instant now = Instant.now();

        try (var connection = dataSource.getConnection()) {
            return createInTransaction(
                connection,
                new Group(groupId, adminUserId, name, imageUrl, now, now)
            );
        } catch (SQLException exception) {
            if ("23505".equals(exception.getSQLState())) {
                throw new ConflictException("Você já participa de um grupo.");
            }
            throw new IllegalStateException("Could not create group", exception);
        }
    }

    private Group createInTransaction(Connection connection, Group group)
        throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            insertGroup(connection, group);
            insertAdminAsMember(connection, group);
            connection.commit();
            return group;
        } catch (SQLException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private void insertGroup(Connection connection, Group group)
        throws SQLException {
        try (var statement = connection.prepareStatement(INSERT_GROUP)) {
            statement.setObject(1, group.id());
            statement.setLong(2, group.adminUserId());
            statement.setString(3, group.name());
            statement.setString(4, group.imageUrl());
            statement.setTimestamp(5, Timestamp.from(group.createdAt()));
            statement.setTimestamp(6, Timestamp.from(group.updatedAt()));
            statement.executeUpdate();
        }
    }

    private void insertAdminAsMember(Connection connection, Group group)
        throws SQLException {
        try (var statement = connection.prepareStatement(INSERT_MEMBER)) {
            statement.setObject(1, group.id());
            statement.setLong(2, group.adminUserId());
            statement.setTimestamp(3, Timestamp.from(group.createdAt()));
            statement.executeUpdate();
        }
    }

    private GroupWithMemberCount mapGroup(ResultSet resultSet)
        throws SQLException {
        Group group = new Group(
            resultSet.getObject("id", UUID.class),
            resultSet.getLong("admin_user_id"),
            resultSet.getString("name"),
            resultSet.getString("image_url"),
            resultSet.getTimestamp("created_at").toInstant(),
            resultSet.getTimestamp("updated_at").toInstant()
        );

        return new GroupWithMemberCount(
            group,
            resultSet.getInt("member_count")
        );
    }

    public record GroupWithMemberCount(Group group, int memberCount) {
    }
}
