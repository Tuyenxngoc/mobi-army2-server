package com.teamobi.mobiarmy2.config;

public interface IRedisConfig {
    String getHost();

    int getPort();

    String getPassword();

    int getMaxTotal();

    int getMaxIdle();

    int getMinIdle();
}
