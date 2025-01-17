package com.teamobi.mobiarmy2.service;

public interface ILoginRateLimiterService {
    long getRemainingLoginTime(String username);

    void saveLogoutTime(String username);
}
