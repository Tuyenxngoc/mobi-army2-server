package com.teamobi.mobiarmy2.config;

import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.Getter;
import lombok.Setter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Getter
@Setter
public class HikariCPConfig {

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
            initConfig();
            validateConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initConfig() {
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
        }
    }

    private void validateConfig() {
        if (Utils.isNullOrEmpty(jdbcUrl)) {
            throw new RuntimeException("Configuration Error: 'jdbc.url' is missing or empty. Please provide a valid JDBC URL in the configuration file.");
        }

        if (Utils.isNullOrEmpty(username)) {
            throw new RuntimeException("Configuration Error: 'jdbc.username' is missing or empty. Please provide a valid database username in the configuration file.");
        }

        if (Utils.isNullOrEmpty(password)) {
            throw new RuntimeException("Configuration Error: 'jdbc.password' is missing or empty. Please provide a valid database password in the configuration file.");
        }

        if (maxPoolSize <= 0) {
            throw new RuntimeException("Configuration Error: 'jdbc.maxPoolSize' must be a positive integer. Please set a valid pool size (e.g., 10) in the configuration file.");
        }

        if (minIdle < 0) {
            throw new RuntimeException("Configuration Error: 'jdbc.minIdle' cannot be negative. Please set a non-negative value (e.g., 2) in the configuration file.");
        }

        if (connectionTimeout <= 0) {
            throw new RuntimeException("Configuration Error: 'jdbc.connectionTimeout' must be a positive integer. Please set a valid timeout (e.g., 30000 ms) in the configuration file.");
        }
    }
}
