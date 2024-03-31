package com.teamobi.mobiarmy2.config;

public interface IDatabaseConfig {

    String getJdbcUrl();

    String getUsername();

    String getPassword();

    int getMaxPoolSize();

    int getMinIdle();

    int getConnectionTimeout();

    boolean isShowSql();

}
