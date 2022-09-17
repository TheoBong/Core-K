package com.bongbong.core.networking.redis;

import com.bongbong.core.CorePlugin;
import lombok.Getter;
import redis.clients.jedis.Jedis;

import java.util.LinkedList;
import java.util.Queue;

public class RedisPublisher {

    private final @Getter Queue<RedisMessage> messageQueue;
    public RedisPublisher(Jedis jedis, CorePlugin plugin) {
        this.messageQueue = new LinkedList<>();

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, ()-> {
            if(!messageQueue.isEmpty()) {
                RedisMessage redisMessage = messageQueue.poll();
                jedis.publish(plugin.getConfig().getString("networking.redis.channel"), redisMessage.getMessage().toString());
            }
        }, 1, 1);
    }
}
