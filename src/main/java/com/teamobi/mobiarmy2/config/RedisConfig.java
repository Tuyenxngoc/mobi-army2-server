package com.teamobi.mobiarmy2.config;

import com.teamobi.mobiarmy2.constant.GameConstants;
import lombok.Getter;
import lombok.Setter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Getter
@Setter
public class RedisConfig {
    private final Properties properties;
    private String host;
    private int port;
    private String password;
    private int maxTotal;
    private int maxIdle;
    private int minIdle;

    public RedisConfig() {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(GameConstants.CONFIG_BASE_URL + "/redis.properties")) {
            properties.load(fis);
            initConfig();
            validateConfig();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void initConfig() {
        try {
            this.host = properties.getProperty("redis.host", "localhost");
            this.port = Integer.parseInt(properties.getProperty("redis.port", "6379"));
            this.password = properties.getProperty("redis.password", null);
            this.maxTotal = Integer.parseInt(properties.getProperty("redis.maxTotal", "128"));
            this.maxIdle = Integer.parseInt(properties.getProperty("redis.maxIdle", "64"));
            this.minIdle = Integer.parseInt(properties.getProperty("redis.minIdle", "16"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void validateConfig() {
        if (host == null || host.isEmpty()) {
            System.err.println("Lỗi: 'redis.host' không được để trống.");
            System.exit(1);
        }

        if (port <= 0 || port > 65535) {
            System.err.println("Lỗi: 'redis.port' phải nằm trong khoảng 1-65535.");
            System.exit(1);
        }

        if (maxTotal <= 0) {
            System.err.println("Lỗi: 'redis.maxTotal' phải lớn hơn 0.");
            System.exit(1);
        }

        if (maxIdle < 0) {
            System.err.println("Lỗi: 'redis.maxIdle' không được âm.");
            System.exit(1);
        }

        if (minIdle < 0) {
            System.err.println("Lỗi: 'redis.minIdle' không được âm.");
            System.exit(1);
        }
    }
}
