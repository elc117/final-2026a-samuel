package com.gymsocial.checkin;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class CheckInRepository {

    private static final String FIND_GROUP_ID_BY_USER_ID = """
        SELECT group_id
        FROM group_members
        WHERE user_id = ?
        """;

    private static final String INSERT = """
        INSERT INTO check_ins (
            id, group_id, author_user_id, title, description, image_url,
            created_at
        )
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String FIND_BY_GROUP_MEMBER = """
        SELECT c.id, c.group_id, c.author_user_id, c.title, c.description,
               c.image_url, c.created_at, u.name AS author_name,
               u.profile_image_url AS author_image_url
        FROM check_ins c
        JOIN users u ON u.id = c.author_user_id
        JOIN group_members viewer ON viewer.group_id = c.group_id
        WHERE viewer.user_id = ?
        ORDER BY c.created_at DESC
        """;

    private static final String FIND_BY_ID = """
        SELECT c.id, c.group_id, c.author_user_id, c.title, c.description,
               c.image_url, c.created_at, u.name AS author_name,
               u.profile_image_url AS author_image_url
        FROM check_ins c
        JOIN users u ON u.id = c.author_user_id
        WHERE c.id = ?
        """;

    private final DataSource dataSource;

    public CheckInRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<UUID> findGroupIdByUserId(long userId) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(FIND_GROUP_ID_BY_USER_ID)
        ) {
            statement.setLong(1, userId);

            try (var resultSet = statement.executeQuery()) {
                return resultSet.next()
                    ? Optional.of(resultSet.getObject("group_id", UUID.class))
                    : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not query check-in group",
                exception
            );
        }
    }

    public void create(CheckIn checkIn) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(INSERT)
        ) {
            statement.setObject(1, checkIn.id());
            statement.setObject(2, checkIn.groupId());
            statement.setLong(3, checkIn.authorUserId());
            statement.setString(4, checkIn.title());
            statement.setString(5, checkIn.description());
            statement.setString(6, checkIn.imageUrl());
            statement.setTimestamp(7, Timestamp.from(checkIn.createdAt()));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not create check-in", exception);
        }
    }

    public List<CheckInWithAuthor> findByGroupMember(long userId) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(FIND_BY_GROUP_MEMBER)
        ) {
            statement.setLong(1, userId);

            try (var resultSet = statement.executeQuery()) {
                List<CheckInWithAuthor> checkIns = new ArrayList<>();

                while (resultSet.next()) {
                    checkIns.add(mapWithAuthor(resultSet));
                }

                return checkIns;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not query check-ins", exception);
        }
    }

    public Optional<CheckInWithAuthor> findById(UUID checkInId) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(FIND_BY_ID)
        ) {
            statement.setObject(1, checkInId);

            try (var resultSet = statement.executeQuery()) {
                return resultSet.next()
                    ? Optional.of(mapWithAuthor(resultSet))
                    : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not query check-in", exception);
        }
    }

    private CheckInWithAuthor mapWithAuthor(ResultSet resultSet)
        throws SQLException {
        var checkIn = new CheckIn(
            resultSet.getObject("id", UUID.class),
            resultSet.getObject("group_id", UUID.class),
            resultSet.getLong("author_user_id"),
            resultSet.getString("title"),
            resultSet.getString("description"),
            resultSet.getString("image_url"),
            resultSet.getTimestamp("created_at").toInstant()
        );

        return new CheckInWithAuthor(
            checkIn,
            resultSet.getString("author_name"),
            resultSet.getString("author_image_url")
        );
    }

    public record CheckInWithAuthor(
        CheckIn checkIn,
        String authorName,
        String authorImageUrl
    ) {
    }
}
