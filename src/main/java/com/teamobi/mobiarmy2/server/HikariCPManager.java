package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.config.HikariCPConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
public class HikariCPManager {

    @FunctionalInterface
    public interface TransactionConsumer {
        void accept(Connection connection) throws SQLException;
    }

    private final HikariCPConfig config;
    private HikariDataSource dataSource;

    public HikariCPManager(HikariCPConfig config) {
        this.config = config;
        initDataSource();
    }

    private void initDataSource() {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());

        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
        hikariConfig.setMinimumIdle(config.getMinIdle());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());

        dataSource = new HikariDataSource(hikariConfig);
        log.info("HikariCP DataSource initialized.");
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            log.warn("DataSource is closed or uninitialized; reinitializing DataSource.");
            initDataSource();
        }
        return dataSource.getConnection();
    }

    public Optional<Integer> update(String sql, Object... params) {
        if (config.isShowSql()) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append(sql).append(" [Parameters: ");
            for (Object param : params) {
                logMessage.append(param).append(", ");
            }
            logMessage.delete(logMessage.length() - 2, logMessage.length());
            logMessage.append("]");
            log.info(logMessage.toString());
        }

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            int rowsUpdated = statement.executeUpdate();
            return Optional.of(rowsUpdated);
        } catch (SQLException e) {
            log.error("SQL Update failed: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public boolean transaction(TransactionConsumer transactionConsumer) {
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            transactionConsumer.accept(connection);
            connection.commit();
            return true;
        } catch (Exception e) {
            log.error("Transaction failed: {}", e.getMessage(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    log.error("Rollback failed: {}", ex.getMessage(), ex);
                }
            }
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    log.error("Failed to close connection after transaction: {}", e.getMessage(), e);
                }
            }
        }
    }

    public int[] executeBatch(String sql, Consumer<PreparedStatement> batchConsumer) {
        if (config.isShowSql()) {
            log.info("Executing batch for SQL: {}", sql);
        }

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            batchConsumer.accept(statement);
            return statement.executeBatch();
        } catch (SQLException e) {
            log.error("Batch execution failed: {}", e.getMessage(), e);
        }
        return new int[0];
    }

    public void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("HikariCP DataSource closed.");
        }
    }
}
