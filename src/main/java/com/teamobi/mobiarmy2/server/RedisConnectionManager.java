package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.config.IRedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnectionManager {

    private final JedisPool jedisPool;

    private RedisConnectionManager() {
        IRedisConfig redisConfig = ApplicationContext.getInstance().getBean(IRedisConfig.class);

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

    public static RedisConnectionManager getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public Jedis getConnection() {
        return jedisPool.getResource();
    }

    public void close() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }

    private static class SingletonHelper {
        private static final RedisConnectionManager INSTANCE = new RedisConnectionManager();
    }
}
