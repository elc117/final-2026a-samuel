package com.gymsocial.challenge;

import com.gymsocial.challenge.dto.ChallengeRankingResponse;
import com.gymsocial.shared.exception.ConflictException;
import com.gymsocial.shared.exception.NotFoundException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ChallengeRepository {

    private static final String FIND_GROUP_BY_MEMBER = """
        SELECT g.id, g.admin_user_id
        FROM groups g
        JOIN group_members member ON member.group_id = g.id
        WHERE member.user_id = ?
        """;

    private static final String END_EXPIRED = """
        UPDATE challenges
        SET status = 'ENDED', ended_at = ?
        WHERE group_id = ? AND status = 'ACTIVE' AND ends_at < ?
        """;

    private static final String FIND_ACTIVE = """
        SELECT id, group_id, creator_user_id, title, description, period,
               allow_multiple_check_ins_per_day, starts_at, ends_at, status,
               created_at
        FROM challenges
        WHERE group_id = ? AND status = 'ACTIVE'
        """;

    private static final String END_ACTIVE = """
        UPDATE challenges
        SET status = 'ENDED', ended_at = ?
        WHERE group_id = ? AND status = 'ACTIVE'
        """;

    private static final String INSERT = """
        INSERT INTO challenges (
            id, group_id, creator_user_id, title, description, period,
            allow_multiple_check_ins_per_day, starts_at, ends_at, status,
            created_at
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?)
        """;

    private static final String FIND_RANKING_MULTIPLE = """
        SELECT u.id, u.name, u.profile_image_url, COUNT(c.id) AS score
        FROM group_members member
        JOIN users u ON u.id = member.user_id
        LEFT JOIN check_ins c
          ON c.group_id = member.group_id
         AND c.author_user_id = member.user_id
         AND c.created_at >= ?
         AND (c.created_at AT TIME ZONE 'America/Sao_Paulo')::date <= ?
        WHERE member.group_id = ?
        GROUP BY u.id
        ORDER BY score DESC, u.name ASC
        """;

    private static final String FIND_RANKING_DAILY = """
        SELECT u.id, u.name, u.profile_image_url,
               COUNT(DISTINCT (
                   c.created_at AT TIME ZONE 'America/Sao_Paulo'
               )::date) AS score
        FROM group_members member
        JOIN users u ON u.id = member.user_id
        LEFT JOIN check_ins c
          ON c.group_id = member.group_id
         AND c.author_user_id = member.user_id
         AND c.created_at >= ?
         AND (c.created_at AT TIME ZONE 'America/Sao_Paulo')::date <= ?
        WHERE member.group_id = ?
        GROUP BY u.id
        ORDER BY score DESC, u.name ASC
        """;

    private final DataSource dataSource;

    public ChallengeRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<GroupAccess> findGroupByMember(long userId) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(FIND_GROUP_BY_MEMBER)
        ) {
            statement.setLong(1, userId);
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next()
                    ? Optional.of(new GroupAccess(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getLong("admin_user_id")
                    ))
                    : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not query challenge group",
                exception
            );
        }
    }

    public Optional<Challenge> findActive(UUID groupId) {
        endExpired(groupId);

        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(FIND_ACTIVE)
        ) {
            statement.setObject(1, groupId);
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next()
                    ? Optional.of(mapChallenge(resultSet))
                    : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not query active challenge",
                exception
            );
        }
    }

    public Challenge create(Challenge challenge) {
        try (var connection = dataSource.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                lockGroup(connection, challenge.groupId());
                endExpired(connection, challenge.groupId());
                if (findActive(connection, challenge.groupId()).isPresent()) {
                    throw new ConflictException(
                        "O grupo já possui um desafio ativo."
                    );
                }
                insert(connection, challenge);
                connection.commit();
                return challenge;
            } catch (RuntimeException | SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            if ("23505".equals(exception.getSQLState())) {
                throw new ConflictException(
                    "O grupo já possui um desafio ativo."
                );
            }
            throw new IllegalStateException("Could not create challenge", exception);
        }
    }

    public List<ChallengeRankingResponse> findRanking(Challenge challenge) {
        String query = challenge.allowMultipleCheckInsPerDay()
            ? FIND_RANKING_MULTIPLE
            : FIND_RANKING_DAILY;

        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(query)
        ) {
            statement.setTimestamp(1, Timestamp.from(challenge.createdAt()));
            statement.setObject(2, challenge.endsAt());
            statement.setObject(3, challenge.groupId());
            try (var resultSet = statement.executeQuery()) {
                List<ChallengeRankingResponse> ranking = new ArrayList<>();
                while (resultSet.next()) {
                    ranking.add(new ChallengeRankingResponse(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("profile_image_url"),
                        resultSet.getInt("score")
                    ));
                }
                return ranking;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not query challenge ranking",
                exception
            );
        }
    }

    public boolean endActive(UUID groupId) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(END_ACTIVE)
        ) {
            statement.setTimestamp(1, Timestamp.from(Instant.now()));
            statement.setObject(2, groupId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not end active challenge",
                exception
            );
        }
    }

    private void endExpired(UUID groupId) {
        try (var connection = dataSource.getConnection()) {
            endExpired(connection, groupId);
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not end expired challenge",
                exception
            );
        }
    }

    private void endExpired(Connection connection, UUID groupId)
        throws SQLException {
        try (var statement = connection.prepareStatement(END_EXPIRED)) {
            statement.setTimestamp(1, Timestamp.from(Instant.now()));
            statement.setObject(2, groupId);
            statement.setObject(3, LocalDate.now());
            statement.executeUpdate();
        }
    }

    private void lockGroup(Connection connection, UUID groupId)
        throws SQLException {
        try (
            var statement = connection.prepareStatement(
                "SELECT id FROM groups WHERE id = ? FOR UPDATE"
            )
        ) {
            statement.setObject(1, groupId);
            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new NotFoundException("Grupo não encontrado.");
                }
            }
        }
    }

    private Optional<Challenge> findActive(
        Connection connection,
        UUID groupId
    ) throws SQLException {
        try (var statement = connection.prepareStatement(FIND_ACTIVE)) {
            statement.setObject(1, groupId);
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next()
                    ? Optional.of(mapChallenge(resultSet))
                    : Optional.empty();
            }
        }
    }

    private void insert(Connection connection, Challenge challenge)
        throws SQLException {
        try (var statement = connection.prepareStatement(INSERT)) {
            statement.setObject(1, challenge.id());
            statement.setObject(2, challenge.groupId());
            statement.setLong(3, challenge.creatorUserId());
            statement.setString(4, challenge.title());
            statement.setString(5, challenge.description());
            statement.setString(6, challenge.period());
            statement.setBoolean(
                7,
                challenge.allowMultipleCheckInsPerDay()
            );
            statement.setObject(8, challenge.startsAt());
            statement.setObject(9, challenge.endsAt());
            statement.setTimestamp(10, Timestamp.from(challenge.createdAt()));
            statement.executeUpdate();
        }
    }

    private Challenge mapChallenge(ResultSet resultSet) throws SQLException {
        return new Challenge(
            resultSet.getObject("id", UUID.class),
            resultSet.getObject("group_id", UUID.class),
            resultSet.getLong("creator_user_id"),
            resultSet.getString("title"),
            resultSet.getString("description"),
            resultSet.getString("period"),
            resultSet.getBoolean("allow_multiple_check_ins_per_day"),
            resultSet.getObject("starts_at", LocalDate.class),
            resultSet.getObject("ends_at", LocalDate.class),
            resultSet.getString("status"),
            resultSet.getTimestamp("created_at").toInstant()
        );
    }

    public record GroupAccess(UUID groupId, long adminUserId) {
    }
}
