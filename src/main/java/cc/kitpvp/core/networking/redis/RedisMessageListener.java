package cc.kitpvp.core.networking.redis;

public interface RedisMessageListener {
    void onReceive(RedisMessage redisMessage);
}
