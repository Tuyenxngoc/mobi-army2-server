package com.teamobi.mobiarmy2.service;

public interface IConnectionBlockerService {
    boolean isIpBlocked(String ipAddress);

    void incrementIpConnectionCount(String ipAddress);

    void decrementIpConnectionCount(String ipAddress);
}
