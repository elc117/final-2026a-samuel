package com.gymsocial.auth;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.OptionalLong;
import java.util.UUID;


public final class RefreshTokenRepository {

    private static final String INSERT = """
        INSERT INTO refresh_token_sessions (
            id, user_id, token_hash, created_at, expires_at
        )
        VALUES (?, ?, ?, ?, ?)
        """;

    private static final String CONSUME = """
        UPDATE refresh_token_sessions
        SET revoked_at = ?, replaced_by_id = ?
        WHERE token_hash = ?
          AND revoked_at IS NULL
          AND expires_at > ?
        RETURNING user_id
        """;

    private static final String REVOKE = """
        UPDATE refresh_token_sessions
        SET revoked_at = ?
        WHERE token_hash = ?
          AND revoked_at IS NULL
        """;

    private final DataSource dataSource;

    public RefreshTokenRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void create(
        UUID id,
        long userId,
        String tokenHash,
        Instant createdAt,
        Instant expiresAt
    ) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(INSERT)
        ) {
            setInsertParameters(
                statement,
                id,
                userId,
                tokenHash,
                createdAt,
                expiresAt
            );
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not create refresh token session",
                exception
            );
        }
    }

    public OptionalLong rotate(
        String currentTokenHash,
        UUID replacementId,
        String replacementTokenHash,
        Instant now,
        Instant replacementExpiresAt
    ) {
        try (var connection = dataSource.getConnection()) {
            return rotateInTransaction(
                connection,
                currentTokenHash,
                replacementId,
                replacementTokenHash,
                now,
                replacementExpiresAt
            );
        }
        catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not rotate refresh token session",
                exception
            );
        }
    }

    public void revoke(String tokenHash, Instant revokedAt) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(REVOKE)
        ) {
            statement.setTimestamp(1, Timestamp.from(revokedAt));
            statement.setString(2, tokenHash);
            statement.executeUpdate();
        }
        catch (SQLException exception) {
            throw new IllegalStateException(
                "Could not revoke refresh token session",
                exception
            );
        }
    }

    private OptionalLong rotateInTransaction(
        Connection connection,
        String currentTokenHash,
        UUID replacementId,
        String replacementTokenHash,
        Instant now,
        Instant replacementExpiresAt
    ) throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            OptionalLong userId = consume(
                connection,
                currentTokenHash,
                replacementId,
                now
            );

            if (userId.isEmpty()) {
                connection.rollback();
                return OptionalLong.empty();
            }

            insert(
                connection,
                replacementId,
                userId.getAsLong(),
                replacementTokenHash,
                now,
                replacementExpiresAt
            );
            connection.commit();
            return userId;
        }
        catch (SQLException exception) {
            connection.rollback();
            throw exception;
        }
        finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private OptionalLong consume(
        Connection connection,
        String tokenHash,
        UUID replacementId,
        Instant now
    ) throws SQLException {
        try (var statement = connection.prepareStatement(CONSUME)) {
            statement.setTimestamp(1, Timestamp.from(now));
            statement.setObject(2, replacementId);
            statement.setString(3, tokenHash);
            statement.setTimestamp(4, Timestamp.from(now));

            try (var resultSet = statement.executeQuery()) {
                return resultSet.next()
                    ? OptionalLong.of(resultSet.getLong("user_id"))
                    : OptionalLong.empty();
            }
        }
    }

    private void insert(
        Connection connection,
        UUID id,
        long userId,
        String tokenHash,
        Instant createdAt,
        Instant expiresAt
    ) throws SQLException {
        try (var statement = connection.prepareStatement(INSERT)) {
            setInsertParameters(
                statement,
                id,
                userId,
                tokenHash,
                createdAt,
                expiresAt
            );
            statement.executeUpdate();
        }
    }

    private void setInsertParameters(
        java.sql.PreparedStatement statement,
        UUID id,
        long userId,
        String tokenHash,
        Instant createdAt,
        Instant expiresAt
    ) throws SQLException {
        statement.setObject(1, id);
        statement.setLong(2, userId);
        statement.setString(3, tokenHash);
        statement.setTimestamp(4, Timestamp.from(createdAt));
        statement.setTimestamp(5, Timestamp.from(expiresAt));
    }
}
