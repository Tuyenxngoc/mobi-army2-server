package com.teamobi.mobiarmy2.config.Impl;

import com.teamobi.mobiarmy2.config.IServerConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerConfig implements IServerConfig {
    private static final String directoryPath = "src/main/resources/";

    private final Properties properties;

    public ServerConfig(String resourceName) {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(directoryPath + resourceName)) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getPort() {
        return Integer.parseInt(properties.getProperty("port"));
    }

    @Override
    public boolean isDebug() {
        return Boolean.parseBoolean(properties.getProperty("debug"));
    }

}
