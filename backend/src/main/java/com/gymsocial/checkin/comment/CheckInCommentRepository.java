package com.gymsocial.checkin.comment;

import com.gymsocial.shared.exception.NotFoundException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CheckInCommentRepository {

    private static final String INSERT = """
        INSERT INTO check_in_comments (
            id, check_in_id, author_user_id, content, created_at
        )
        SELECT ?, c.id, ?, ?, ?
        FROM check_ins c
        JOIN group_members member ON member.group_id = c.group_id
        WHERE c.id = ? AND member.user_id = ?
        """;

    private static final String FIND_BY_CHECK_IN = """
        SELECT comment.id, author.name AS author_name,
               author.profile_image_url AS author_image_url,
               comment.content, comment.created_at
        FROM check_in_comments comment
        JOIN check_ins check_in ON check_in.id = comment.check_in_id
        JOIN group_members viewer ON viewer.group_id = check_in.group_id
        JOIN users author ON author.id = comment.author_user_id
        WHERE comment.check_in_id = ? AND viewer.user_id = ?
        ORDER BY comment.created_at
        """;

    private static final String FIND_BY_ID = """
        SELECT comment.id, author.name AS author_name,
               author.profile_image_url AS author_image_url,
               comment.content, comment.created_at
        FROM check_in_comments comment
        JOIN users author ON author.id = comment.author_user_id
        WHERE comment.id = ?
        """;

    private final DataSource dataSource;

    public CheckInCommentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<CommentResult> findByCheckIn(UUID checkInId, long userId) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(FIND_BY_CHECK_IN)
        ) {
            statement.setObject(1, checkInId);
            statement.setLong(2, userId);

            try (var resultSet = statement.executeQuery()) {
                List<CommentResult> comments = new ArrayList<>();
                while (resultSet.next()) {
                    comments.add(new CommentResult(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getString("author_name"),
                        resultSet.getString("author_image_url"),
                        resultSet.getString("content"),
                        resultSet.getTimestamp("created_at").toInstant()
                    ));
                }
                return comments;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not query comments", exception);
        }
    }

    public CommentResult create(UUID checkInId, long userId, String content) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(INSERT)
        ) {
            statement.setObject(1, id);
            statement.setLong(2, userId);
            statement.setString(3, content);
            statement.setTimestamp(4, Timestamp.from(now));
            statement.setObject(5, checkInId);
            statement.setLong(6, userId);

            if (statement.executeUpdate() == 0) {
                throw new NotFoundException("Check-in não encontrado.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not create comment", exception);
        }

        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(FIND_BY_ID)
        ) {
            statement.setObject(1, id);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return new CommentResult(
                    resultSet.getObject("id", UUID.class),
                    resultSet.getString("author_name"),
                    resultSet.getString("author_image_url"),
                    resultSet.getString("content"),
                    resultSet.getTimestamp("created_at").toInstant()
                );
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load comment", exception);
        }
    }

    public record CommentResult(
        UUID id,
        String authorName,
        String authorImageKey,
        String content,
        Instant createdAt
    ) {
    }
}
