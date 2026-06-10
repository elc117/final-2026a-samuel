package com.gymsocial.chat;

import com.gymsocial.shared.pagination.InstantUuidCursor;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ChatRepository {

    private static final String FIND_SESSION = """
        SELECT gm.group_id, u.id AS user_id, u.name AS user_name,
               u.profile_image_url AS user_image_url
        FROM group_members gm
        JOIN users u ON u.id = gm.user_id
        WHERE gm.user_id = ?
        """;

    private static final String INSERT_FOR_GROUP_MEMBER = """
        INSERT INTO group_chat_messages (
            id, group_id, author_user_id, content, created_at
        )
        SELECT ?, gm.group_id, gm.user_id, ?, ?
        FROM group_members gm
        WHERE gm.user_id = ?
        """;

    private static final String FIND_BY_ID_FOR_GROUP_MEMBER = """
        SELECT m.id, m.group_id, m.author_user_id, m.content, m.created_at,
               u.name AS author_name,
               u.profile_image_url AS author_image_url
        FROM group_chat_messages m
        JOIN users u ON u.id = m.author_user_id
        JOIN group_members viewer ON viewer.group_id = m.group_id
        WHERE m.id = ? AND viewer.user_id = ?
        """;

    private static final String FIND_FIRST_PAGE = """
        SELECT m.id, m.group_id, m.author_user_id, m.content, m.created_at,
               u.name AS author_name,
               u.profile_image_url AS author_image_url
        FROM group_chat_messages m
        JOIN users u ON u.id = m.author_user_id
        JOIN group_members viewer ON viewer.group_id = m.group_id
        WHERE viewer.user_id = ?
        ORDER BY m.created_at DESC, m.id DESC
        LIMIT ?
        """;

    private static final String FIND_PAGE = """
        SELECT m.id, m.group_id, m.author_user_id, m.content, m.created_at,
               u.name AS author_name,
               u.profile_image_url AS author_image_url
        FROM group_chat_messages m
        JOIN users u ON u.id = m.author_user_id
        JOIN group_members viewer ON viewer.group_id = m.group_id
        WHERE viewer.user_id = ?
          AND (
              m.created_at < ? OR
              (m.created_at = ? AND m.id < ?)
          )
        ORDER BY m.created_at DESC, m.id DESC
        LIMIT ?
        """;

    private final DataSource dataSource;

    public ChatRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<ChatSession> findSession(long userId) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(FIND_SESSION)
        ) {
            statement.setLong(1, userId);

            try (var resultSet = statement.executeQuery()) {
                return resultSet.next()
                    ? Optional.of(new ChatSession(
                        resultSet.getObject("group_id", UUID.class),
                        resultSet.getLong("user_id"),
                        resultSet.getString("user_name"),
                        resultSet.getString("user_image_url")
                    ))
                    : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not query chat session",
                exception
            );
        }
    }

    public boolean createForGroupMember(
        UUID messageId,
        long userId,
        String content,
        Instant createdAt
    ) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(
                INSERT_FOR_GROUP_MEMBER
            )
        ) {
            statement.setObject(1, messageId);
            statement.setString(2, content);
            statement.setTimestamp(3, Timestamp.from(createdAt));
            statement.setLong(4, userId);
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not create chat message",
                exception
            );
        }
    }

    public Optional<ChatMessage> findByIdForGroupMember(
        UUID messageId,
        long userId
    ) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(
                FIND_BY_ID_FOR_GROUP_MEMBER
            )
        ) {
            statement.setObject(1, messageId);
            statement.setLong(2, userId);

            try (var resultSet = statement.executeQuery()) {
                return resultSet.next()
                    ? Optional.of(mapMessage(resultSet))
                    : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not query chat message",
                exception
            );
        }
    }

    public List<ChatMessage> findPage(
        long userId,
        InstantUuidCursor cursor,
        int limit
    ) {
        String query = cursor == null ? FIND_FIRST_PAGE : FIND_PAGE;

        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(query)
        ) {
            statement.setLong(1, userId);
            if (cursor == null) {
                statement.setInt(2, limit);
            } else {
                statement.setTimestamp(2, Timestamp.from(cursor.createdAt()));
                statement.setTimestamp(3, Timestamp.from(cursor.createdAt()));
                statement.setObject(4, cursor.id());
                statement.setInt(5, limit);
            }

            try (var resultSet = statement.executeQuery()) {
                List<ChatMessage> messages = new ArrayList<>();
                while (resultSet.next()) {
                    messages.add(mapMessage(resultSet));
                }
                return messages;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not query chat messages",
                exception
            );
        }
    }

    private ChatMessage mapMessage(ResultSet resultSet) throws SQLException {
        return new ChatMessage(
            resultSet.getObject("id", UUID.class),
            resultSet.getObject("group_id", UUID.class),
            resultSet.getLong("author_user_id"),
            resultSet.getString("author_name"),
            resultSet.getString("author_image_url"),
            resultSet.getString("content"),
            resultSet.getTimestamp("created_at").toInstant()
        );
    }

    public record ChatSession(
        UUID groupId,
        long userId,
        String userName,
        String userImageUrl
    ) {
    }

    public record ChatMessage(
        UUID id,
        UUID groupId,
        long authorUserId,
        String authorName,
        String authorImageUrl,
        String content,
        Instant createdAt
    ) {
    }

}
