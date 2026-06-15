package com.gymsocial.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

public final class DatabaseConfig {

    private DatabaseConfig() {
    }

    public static HikariDataSource createDataSource(ApplicationConfig appConfig) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(appConfig.databaseUrl());
        hikariConfig.setUsername(appConfig.databaseUser());
        hikariConfig.setPassword(appConfig.databasePassword());
        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.setMinimumIdle(0);
        hikariConfig.setConnectionTimeout(10_000);
        hikariConfig.setPoolName("gym-social-pool");
        hikariConfig.addDataSourceProperty("tcpKeepAlive", "true");

        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        Flyway.configure()
            .dataSource(dataSource)
            .load()
            .migrate();

        return dataSource;
    }
}
