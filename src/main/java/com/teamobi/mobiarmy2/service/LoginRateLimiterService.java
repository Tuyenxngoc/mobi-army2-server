package com.teamobi.mobiarmy2.service;

import com.teamobi.mobiarmy2.server.RedisConnectionManager;
import redis.clients.jedis.Jedis;

public class LoginRateLimiterService {
    private static final int LOGIN_BLOCK_DURATION = 20;
    private final RedisConnectionManager redisConnectionManager;

    public LoginRateLimiterService(RedisConnectionManager redisConnectionManager) {
        this.redisConnectionManager = redisConnectionManager;
    }

    private static String generateKey(String username) {
        return "user:logout:" + username;
    }

    public long getRemainingLoginTime(String username) {
        try (Jedis jedis = redisConnectionManager.getConnection()) {
            String logoutKey = generateKey(username);
            if (jedis.exists(logoutKey)) {
                return jedis.ttl(logoutKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void saveLogoutTime(String username) {
        try (Jedis jedis = redisConnectionManager.getConnection()) {
            String logoutKey = generateKey(username);

            jedis.set(logoutKey, "blocked");
            jedis.expire(logoutKey, LOGIN_BLOCK_DURATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
