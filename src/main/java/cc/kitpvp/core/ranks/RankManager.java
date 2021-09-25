package cc.kitpvp.core.ranks;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.networking.CoreRedisAction;
import cc.kitpvp.core.networking.mongo.Mongo;
import cc.kitpvp.core.networking.mongo.MongoDeserializedResult;
import cc.kitpvp.core.networking.mongo.MongoUpdate;
import cc.kitpvp.core.networking.redis.RedisMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RankManager {
    private final Core core;
    private @Getter Map<UUID, Rank> ranks;
    public RankManager (Core core) {
        this.core = core;
        this.ranks = new HashMap<>();

        Mongo mongo = core.getMongo();
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

        Rank rank = new Rank(UUID.randomUUID());
        rank.setName(ChatColor.stripColor(name.toLowerCase()));
        rank.setDisplayName(name);
        rank.setWeight(weight);
        ranks.put(rank.getUuid(), rank);
        push(true, rank);
        return rank;
    }

    public void pull(boolean async, UUID uuid, MongoDeserializedResult mdr) {
        core.getMongo().getDocument(async, "core_ranks", uuid, document -> {
            if(document != null) {
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                Rank rank = gson.fromJson(document.getString("elements"), Rank.class);
                ranks.put(rank.getUuid(), rank);
                mdr.call(rank);
            } else {
                mdr.call(null);
            }
        });

        core.getProfileManager().update();
    }

    public void push(boolean async, Rank rank) {
        MongoUpdate mu = new MongoUpdate("core_ranks", rank.getUuid());
        mu.put("elements", rank.serialize());
        core.getMongo().massUpdate(async, mu);

        JsonObject json = new JsonObject();
        json.addProperty("action", CoreRedisAction.RANK_UPDATE.toString());
        json.addProperty("fromServer", core.getConfig().getString("general.server_name"));
        json.addProperty("rank", rank.getUuid().toString());

        core.getRedisPublisher().getMessageQueue().add(new RedisMessage("core", json));

        core.getProfileManager().update();
    }

    public void remove(boolean async, Rank rank) {
        core.getMongo().deleteDocument(async, "core_ranks", rank.getUuid());

        JsonObject json = new JsonObject();
        json.addProperty("action", CoreRedisAction.RANK_DELETE.toString());
        json.addProperty("fromServer", core.getConfig().getString("general.server_name"));
        json.addProperty("rank", rank.getUuid().toString());

        core.getRedisPublisher().getMessageQueue().add(new RedisMessage("core", json));

        ranks.remove(rank.getUuid());

        core.getProfileManager().update();
    }
}
