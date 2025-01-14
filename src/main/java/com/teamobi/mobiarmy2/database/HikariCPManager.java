package com.teamobi.mobiarmy2.database;

import com.teamobi.mobiarmy2.config.IDatabaseConfig;
import com.teamobi.mobiarmy2.server.ApplicationContext;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

/**
 * @author tuyen
 */
public class HikariCPManager {
    private static final Logger logger = LoggerFactory.getLogger(HikariCPManager.class);

    private final IDatabaseConfig config;
    private HikariDataSource dataSource;

    private HikariCPManager() {
        this.config = ApplicationContext.getInstance().getBean(IDatabaseConfig.class);
        initDataSource();
    }

    private static class SingletonHelper {
        private static final HikariCPManager INSTANCE = new HikariCPManager();
    }

    public static HikariCPManager getInstance() {
        return SingletonHelper.INSTANCE;
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
        logger.info("HikariCP DataSource initialized.");
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            logger.warn("DataSource is closed or uninitialized; reinitializing DataSource.");
            initDataSource();
        }
        return dataSource.getConnection();
    }

    public Optional<Integer> update(String sql, Object... params) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            int rowsUpdated = statement.executeUpdate();
            return Optional.of(rowsUpdated);
        } catch (SQLException e) {
            logger.error("SQL Update failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("HikariCP DataSource closed.");
        }
    }
}
