package com.bongbong.core.networking.redis;

public interface RedisMessageListener {
    void onReceive(RedisMessage redisMessage);
}
