package com.teamobi.mobiarmy2.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class HikariCPConfig {

    private final Properties properties;

    public HikariCPConfig() {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/database.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getJdbcUrl() {
        return properties.getProperty("jdbc.url");
    }

    public String getUsername() {
        return properties.getProperty("jdbc.username");
    }

    public String getPassword() {
        return properties.getProperty("jdbc.password");
    }

    public int getMaxPoolSize() {
        return Integer.parseInt(properties.getProperty("jdbc.maxPoolSize"));
    }

    public int getMinIdle() {
        return Integer.parseInt(properties.getProperty("jdbc.minIdle"));
    }

    public int getConnectionTimeout() {
        return Integer.parseInt(properties.getProperty("jdbc.connectionTimeout"));
    }
}
