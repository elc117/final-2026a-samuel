package com.gymsocial.friendship.database;


import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface TransactionCallback<T> {
    T execute(Connection connection) throws SQLException;
}
