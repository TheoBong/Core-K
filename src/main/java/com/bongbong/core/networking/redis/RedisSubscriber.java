package com.bongbong.core.networking.redis;

import com.bongbong.core.CorePlugin;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.HashSet;
import java.util.Set;

public class RedisSubscriber {

    private Jedis jedis;
    private final CorePlugin plugin;

    private JedisPubSub jedisPubSub;
    private @Getter Set<RedisMessageListener> listeners;
    private String rChannel;
    public RedisSubscriber(Jedis jedis, CorePlugin plugin) {
        this.jedis = jedis;
        this.plugin = plugin;
        this.listeners = new HashSet<>();

        this.rChannel = plugin.getConfig().getString("networking.redis.channel");

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

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, ()-> {
           jedis.subscribe(jedisPubSub, rChannel);
        });
    }
}