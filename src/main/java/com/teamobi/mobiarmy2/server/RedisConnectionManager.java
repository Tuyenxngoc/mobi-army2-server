package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.config.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnectionManager {
    private final JedisPool jedisPool;

    public RedisConnectionManager(RedisConfig redisConfig) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(redisConfig.getMaxTotal());
        poolConfig.setMaxIdle(redisConfig.getMaxIdle());
        poolConfig.setMinIdle(redisConfig.getMinIdle());
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);

        jedisPool = new JedisPool(
                poolConfig,
                redisConfig.getHost(),
                redisConfig.getPort(),
                2000,
                redisConfig.getPassword()
        );
    }

    public Jedis getConnection() {
        return jedisPool.getResource();
    }

    public void close() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
