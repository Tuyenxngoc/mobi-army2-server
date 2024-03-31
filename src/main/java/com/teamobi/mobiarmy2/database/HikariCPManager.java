package com.teamobi.mobiarmy2.database;

import com.teamobi.mobiarmy2.config.Impl.HikariCPConfig;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HikariCPManager {

    private static HikariCPManager instance;
    private final HikariDataSource dataSource;
    private final boolean isShowSql;

    private HikariCPManager() {
        HikariCPConfig config = new HikariCPConfig();
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());

        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
        hikariConfig.setMinimumIdle(config.getMinIdle());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());

        dataSource = new HikariDataSource(hikariConfig);
        this.isShowSql = config.isShowSql();
    }

    public static synchronized HikariCPManager getInstance() {
        if (instance == null) {
            instance = new HikariCPManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public int update(String sql, Object... params) {
        if (isShowSql) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append(sql).append(" [Parameters: ");
            for (Object param : params) {
                logMessage.append(param).append(", ");
            }
            logMessage.delete(logMessage.length() - 2, logMessage.length()); // Remove the trailing comma and space
            logMessage.append("]");
            ServerManager.getInstance().logger().logMessage(logMessage.toString());
        }

        int rowsUpdated = 0;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            rowsUpdated = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowsUpdated;
    }

    public void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
