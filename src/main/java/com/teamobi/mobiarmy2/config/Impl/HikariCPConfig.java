package com.teamobi.mobiarmy2.config.Impl;

import com.teamobi.mobiarmy2.config.IDatabaseConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class HikariCPConfig implements IDatabaseConfig {

    private final Properties properties;

    public HikariCPConfig() {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/database.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getJdbcUrl() {
        return properties.getProperty("jdbc.url");
    }

    @Override
    public String getUsername() {
        return properties.getProperty("jdbc.username");
    }

    @Override
    public String getPassword() {
        return properties.getProperty("jdbc.password");
    }

    @Override
    public int getMaxPoolSize() {
        return Integer.parseInt(properties.getProperty("jdbc.maxPoolSize"));
    }

    @Override
    public int getMinIdle() {
        return Integer.parseInt(properties.getProperty("jdbc.minIdle"));
    }

    @Override
    public int getConnectionTimeout() {
        return Integer.parseInt(properties.getProperty("jdbc.connectionTimeout"));
    }

}
