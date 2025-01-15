package com.teamobi.mobiarmy2.config.impl;

import com.teamobi.mobiarmy2.config.IDatabaseConfig;
import com.teamobi.mobiarmy2.constant.GameConstants;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author tuyen
 */
public class HikariCPConfig implements IDatabaseConfig {

    private final Properties properties;
    private String jdbcUrl;
    private String username;
    private String password;
    private int maxPoolSize;
    private int minIdle;
    private int connectionTimeout;
    private boolean isShowSql;

    public HikariCPConfig() {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(GameConstants.CONFIG_BASE_URL + "/database.properties")) {
            properties.load(fis);
            initializeConfigProperties();
            validateConfigProperties();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void initializeConfigProperties() {
        try {
            this.jdbcUrl = properties.getProperty("jdbc.url");
            this.username = properties.getProperty("jdbc.username");
            this.password = properties.getProperty("jdbc.password");
            this.maxPoolSize = Integer.parseInt(properties.getProperty("jdbc.maxPoolSize", "10"));
            this.minIdle = Integer.parseInt(properties.getProperty("jdbc.minIdle", "2"));
            this.connectionTimeout = Integer.parseInt(properties.getProperty("jdbc.connectionTimeout", "30000"));
            this.isShowSql = Boolean.parseBoolean(properties.getProperty("jdbc.showSql", "false"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void validateConfigProperties() {
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            System.err.println("Configuration Error: 'jdbc.url' is missing or empty. Please provide a valid JDBC URL in the configuration file.");
            System.exit(1);
        }

        if (username == null || username.isEmpty()) {
            System.err.println("Configuration Error: 'jdbc.username' is missing or empty. Please provide a valid database username in the configuration file.");
            System.exit(1);
        }

        if (password == null || password.isEmpty()) {
            System.err.println("Configuration Error: 'jdbc.password' is missing or empty. Please provide a valid database password in the configuration file.");
            System.exit(1);
        }

        if (maxPoolSize <= 0) {
            System.err.println("Configuration Error: 'jdbc.maxPoolSize' must be a positive integer. Please set a valid pool size (e.g., 10) in the configuration file.");
            System.exit(1);
        }

        if (minIdle < 0) {
            System.err.println("Configuration Error: 'jdbc.minIdle' cannot be negative. Please set a non-negative value (e.g., 2) in the configuration file.");
            System.exit(1);
        }

        if (connectionTimeout <= 0) {
            System.err.println("Configuration Error: 'jdbc.connectionTimeout' must be a positive integer. Please set a valid timeout (e.g., 30000 ms) in the configuration file.");
            System.exit(1);
        }
    }

    @Override
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    @Override
    public int getMinIdle() {
        return minIdle;
    }

    @Override
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public boolean isShowSql() {
        return isShowSql;
    }
}
