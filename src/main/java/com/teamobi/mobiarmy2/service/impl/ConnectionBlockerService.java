package com.teamobi.mobiarmy2.service.impl;

import com.teamobi.mobiarmy2.server.RedisConnectionManager;
import com.teamobi.mobiarmy2.service.IConnectionBlockerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class ConnectionBlockerService implements IConnectionBlockerService {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionBlockerService.class);
    private static final int MAX_CONNECTIONS_PER_IP = 10;
    private static final int IP_BLOCK_DURATION = 3600;

    private String getKey(String ipAddress) {
        return "ip:" + ipAddress;
    }

    @Override
    public boolean isIpBlocked(String ipAddress) {
        try (Jedis jedis = RedisConnectionManager.getInstance().getConnection()) {
            String key = getKey(ipAddress);
            String countStr = jedis.get(key);
            if (countStr != null) {
                int count = Integer.parseInt(countStr);
                return count >= MAX_CONNECTIONS_PER_IP;
            }
        } catch (Exception e) {
            logger.error("Error checking IP connection count in Redis", e);
        }
        return false;
    }

    @Override
    public void incrementIpConnectionCount(String ipAddress) {
        try (Jedis jedis = RedisConnectionManager.getInstance().getConnection()) {
            String key = getKey(ipAddress);
            jedis.incr(key);
            jedis.expire(key, IP_BLOCK_DURATION);
        } catch (Exception e) {
            logger.error("Error incrementing IP connection count in Redis", e);
        }
    }

    @Override
    public void decrementIpConnectionCount(String ipAddress) {
        try (Jedis jedis = RedisConnectionManager.getInstance().getConnection()) {
            String key = getKey(ipAddress);
            long count = jedis.decr(key);
            if (count <= 0) {
                jedis.del(key);
            }
        } catch (Exception e) {
            logger.error("Error decrementing IP connection count in Redis", e);
        }
    }
}
