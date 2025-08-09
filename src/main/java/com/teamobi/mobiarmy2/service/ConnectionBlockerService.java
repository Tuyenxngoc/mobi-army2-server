package com.teamobi.mobiarmy2.service;

import com.teamobi.mobiarmy2.server.RedisConnectionManager;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

@Slf4j
public class ConnectionBlockerService {
    private static final int MAX_CONNECTIONS_PER_IP = 10;
    private static final int IP_BLOCK_DURATION = 3600;
    private final RedisConnectionManager redisConnectionManager;

    public ConnectionBlockerService(RedisConnectionManager redisConnectionManager) {
        this.redisConnectionManager = redisConnectionManager;
    }

    private String getKey(String ipAddress) {
        return "ip:" + ipAddress;
    }

    public boolean isIpBlocked(String ipAddress) {
        try (Jedis jedis = redisConnectionManager.getConnection()) {
            String key = getKey(ipAddress);
            String countStr = jedis.get(key);
            if (countStr != null) {
                int count = Integer.parseInt(countStr);
                return count >= MAX_CONNECTIONS_PER_IP;
            }
        } catch (Exception e) {
            log.error("Error checking IP connection count in Redis", e);
        }
        return false;
    }

    public void incrementIpConnectionCount(String ipAddress) {
        try (Jedis jedis = redisConnectionManager.getConnection()) {
            String key = getKey(ipAddress);
            jedis.incr(key);
            jedis.expire(key, IP_BLOCK_DURATION);
        } catch (Exception e) {
            log.error("Error incrementing IP connection count in Redis", e);
        }
    }

    public void decrementIpConnectionCount(String ipAddress) {
        try (Jedis jedis = redisConnectionManager.getConnection()) {
            String key = getKey(ipAddress);
            long count = jedis.decr(key);
            if (count <= 0) {
                jedis.del(key);
            }
        } catch (Exception e) {
            log.error("Error decrementing IP connection count in Redis", e);
        }
    }
}
