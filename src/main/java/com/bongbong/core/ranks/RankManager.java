package com.bongbong.core.ranks;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.networking.CoreRedisAction;
import com.bongbong.core.networking.mongo.Mongo;
import com.bongbong.core.networking.mongo.MongoDeserializedResult;
import com.bongbong.core.networking.mongo.MongoUpdate;
import com.bongbong.core.networking.redis.RedisMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RankManager {
    private final CorePlugin plugin;
    private @Getter Map<UUID, Rank> ranks;
    public RankManager (CorePlugin plugin) {
        this.plugin = plugin;
        this.ranks = new HashMap<>();

        Mongo mongo = plugin.getMongo();
        mongo.createCollection(false, "core_ranks");
        mongo.getCollectionIterable(false, "core_ranks",
                iterable -> iterable.forEach(
                document ->
                pull(true, document.get("_id", UUID.class),
                obj -> {})));
    }

    public Rank getRank(UUID uuid) {
        return ranks.get(uuid);
    }

    public Rank getRank(String name) {
        for(Rank rank : ranks.values()) {
            if(rank.getName().equalsIgnoreCase(name)) {
                return rank;
            }
        }

        return null;
    }

    public Rank getDefaultRank() {
        for(Rank rank : ranks.values()) {
            if(rank.isDefaultRank()) {
                return rank;
            }
        }

        return null;
    }

    public Rank createRank(String name, int weight) {
        for(Rank rank : ranks.values()) {
            if(rank.getName().equalsIgnoreCase(name) || rank.getWeight() == weight) {
                return null;
            }
        }

        Rank rank = new Rank(plugin, UUID.randomUUID());
        rank.setName(ChatColor.stripColor(name.toLowerCase()));
        rank.setDisplayName(name);
        rank.setWeight(weight);
        ranks.put(rank.getUuid(), rank);
        push(true, rank);
        return rank;
    }

    public void pull(boolean async, UUID uuid, MongoDeserializedResult mdr) {
        plugin.getMongo().getDocument(async, "core_ranks", uuid, document -> {
            if(document != null) {
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                Rank rank = gson.fromJson(document.getString("elements"), Rank.class);
                ranks.put(rank.getUuid(), rank);
                mdr.call(rank);
            } else {
                mdr.call(null);
            }
        });

        plugin.getProfileManager().update();
    }

    public void push(boolean async, Rank rank) {
        MongoUpdate mu = new MongoUpdate("core_ranks", rank.getUuid());
        mu.put("elements", rank.serialize());
        plugin.getMongo().massUpdate(async, mu);

        JsonObject json = new JsonObject();
        json.addProperty("action", CoreRedisAction.RANK_UPDATE.toString());
        json.addProperty("fromServer", plugin.getConfig().getString("general.server_name"));
        json.addProperty("rank", rank.getUuid().toString());

        plugin.getRedisPublisher().getMessageQueue().add(new RedisMessage("core", json));

        plugin.getProfileManager().update();
    }

    public void remove(boolean async, Rank rank) {
        plugin.getMongo().deleteDocument(async, "core_ranks", rank.getUuid());

        JsonObject json = new JsonObject();
        json.addProperty("action", CoreRedisAction.RANK_DELETE.toString());
        json.addProperty("fromServer", plugin.getConfig().getString("general.server_name"));
        json.addProperty("rank", rank.getUuid().toString());

        plugin.getRedisPublisher().getMessageQueue().add(new RedisMessage("core", json));

        ranks.remove(rank.getUuid());

        plugin.getProfileManager().update();
    }
}
