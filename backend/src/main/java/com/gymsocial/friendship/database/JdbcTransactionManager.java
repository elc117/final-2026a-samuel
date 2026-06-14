package com.gymsocial.friendship.database;

import javax.sql.DataSource;
import java.sql.Connection;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;


public class JdbcTransactionManager {
    private final DataSource dataSource;

    public JdbcTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T run(TransactionCallback<T> callback) {
        try (Connection connection = dataSource.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                T result = callback.execute(connection);
                connection.commit();

                return result;
            }
            catch (RuntimeException | SQLException exception) {
                connection.rollback();
                throw exception;
            }
            finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
        catch (SQLException exception) {
            throw new IllegalStateException(
                    "Database transaction failed",
                    exception
            );
        }
    }
}
