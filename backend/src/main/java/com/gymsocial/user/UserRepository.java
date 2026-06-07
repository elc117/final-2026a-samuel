package com.gymsocial.user;

import com.gymsocial.shared.exception.ConflictException;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public final class UserRepository {

    private static final String FIND_BY_EMAIL = """
        SELECT id, name, username, email, password_hash, profile_image_url,
               status, created_at, updated_at
        FROM users
        WHERE LOWER(email) = LOWER(?)
        """;

    private static final String EXISTS_BY_EMAIL = """
        SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(email) = LOWER(?))
        """;

    private static final String EXISTS_BY_USERNAME = """
        SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(username) = LOWER(?))
        """;

    private static final String INSERT = """
        INSERT INTO users (
            id, name, username, email, password_hash, status, created_at, updated_at
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private final DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<User> findByEmail(String email) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(FIND_BY_EMAIL)
        ) {
            statement.setString(1, email);

            try (var resultSet = statement.executeQuery()) {
                return resultSet.next()
                    ? Optional.of(mapUser(resultSet))
                    : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not query user", exception);
        }
    }

    public boolean existsByEmail(String email) {
        return exists(EXISTS_BY_EMAIL, email);
    }

    public boolean existsByUsername(String username) {
        return exists(EXISTS_BY_USERNAME, username);
    }

    public void save(User user) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(INSERT)
        ) {
            statement.setObject(1, user.id());
            statement.setString(2, user.name());
            statement.setString(3, user.username());
            statement.setString(4, user.email());
            statement.setString(5, user.passwordHash());
            statement.setString(6, user.status());
            statement.setTimestamp(7, Timestamp.from(user.createdAt()));
            statement.setTimestamp(8, Timestamp.from(user.updatedAt()));
            statement.executeUpdate();
        } catch (SQLException exception) {
            if ("23505".equals(exception.getSQLState())) {
                throw new ConflictException("E-mail ou usuário já cadastrado.");
            }
            throw new IllegalStateException("Could not save user", exception);
        }
    }

    private boolean exists(String query, String value) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, value);

            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBoolean(1);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not check user existence", exception);
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        return new User(
            resultSet.getObject("id", java.util.UUID.class),
            resultSet.getString("name"),
            resultSet.getString("username"),
            resultSet.getString("email"),
            resultSet.getString("password_hash"),
            resultSet.getString("profile_image_url"),
            resultSet.getString("status"),
            resultSet.getTimestamp("created_at").toInstant(),
            resultSet.getTimestamp("updated_at").toInstant()
        );
    }
}
