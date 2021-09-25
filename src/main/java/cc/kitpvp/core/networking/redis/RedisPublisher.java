package cc.kitpvp.core.networking.redis;

import cc.kitpvp.core.Core;
import lombok.Getter;
import redis.clients.jedis.Jedis;

import java.util.LinkedList;
import java.util.Queue;

public class RedisPublisher {

    private Jedis jedis;
    private final Core core;
    private @Getter Queue<RedisMessage> messageQueue;
    private boolean running;
    public RedisPublisher(Jedis jedis, Core core) {
        this.jedis = jedis;
        this.core = core;
        this.messageQueue = new LinkedList<>();

        core.getServer().getScheduler().runTaskTimerAsynchronously(core, ()-> {
            if(!messageQueue.isEmpty()) {
                RedisMessage redisMessage = messageQueue.poll();
                jedis.publish(core.getConfig().getString("networking.redis.channel"), redisMessage.getMessage().toString());
            }
        }, 1, 1);
    }
}
