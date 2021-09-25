package cc.kitpvp.core.networking.redis;

import cc.kitpvp.core.Core;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.HashSet;
import java.util.Set;

public class RedisSubscriber {

    private Jedis jedis;
    private final Core core;

    private JedisPubSub jedisPubSub;
    private @Getter Set<RedisMessageListener> listeners;
    private String rChannel;
    public RedisSubscriber(Jedis jedis, Core core) {
        this.jedis = jedis;
        this.core = core;
        this.listeners = new HashSet<>();

        this.rChannel = core.getConfig().getString("networking.redis.channel");

        this.jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if(rChannel.equals(channel)) {
                    for (RedisMessageListener listener : listeners) {
                        listener.onReceive(new RedisMessage(message));
                    }
                }
            }
        };

        core.getServer().getScheduler().runTaskAsynchronously(core, ()-> {
           jedis.subscribe(jedisPubSub, rChannel);
        });
    }
}
