package com.bongbong.core.tags;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.networking.CoreRedisAction;
import com.bongbong.core.networking.mongo.Mongo;
import com.bongbong.core.networking.mongo.MongoDeserializedResult;
import com.bongbong.core.networking.mongo.MongoUpdate;
import com.bongbong.core.networking.redis.RedisMessage;
import com.bongbong.core.utils.ThreadUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TagManager {

    private final CorePlugin plugin;
    private @Getter Map<UUID, Tag> tags;
    public TagManager(CorePlugin plugin) {
        this.plugin = plugin;
        this.tags = new HashMap<>();

        Mongo mongo = plugin.getMongo();
        mongo.createCollection(false, "core_tags");
        mongo.getCollectionIterable(false, "core_tags",
                iterable -> iterable.forEach(
                document ->
                pull(true, document.get("_id", UUID.class),
                obj -> {})));
    }

    public Tag getTag(UUID uuid) {
        return tags.get(uuid);
    }

    public Tag getTag(String name) {
        for(Tag tag : tags.values()) {
            if(tag.getName().equalsIgnoreCase(name)) {
                return tag;
            }
        }
        return null;
    }

    public Tag createTag(String name, String tagSuffix) {
        for(Tag tag : tags.values()) {
            if(tag.getName().equalsIgnoreCase(name)) {
                return null;
            }
        }

        Tag tag = new Tag(UUID.randomUUID());
        tag.setName(name.toLowerCase());
        tag.setDisplayName(name);
        tag.setTag(tagSuffix);

        tags.put(tag.getUuid(), tag);

        push(true, tag);

        return tag;
    }

    public void pull(boolean async, UUID uuid, MongoDeserializedResult mdr) {
        plugin.getMongo().getDocument(async, "core_tags", "_id", uuid, document -> {
            if(document != null) {
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                Tag tag = gson.fromJson(document.getString("elements"), Tag.class);
                tags.put(uuid, tag);
                mdr.call(tag);
            } else {
                mdr.call(null);
            }
        });

        plugin.getProfileManager().update();
    }

    public void push(boolean async, Tag tag) {
        ThreadUtil.runTask(async, plugin, () -> {
            MongoUpdate mu = new MongoUpdate("core_tags", tag.getUuid());
            mu.put("elements", tag.serialize());
            plugin.getMongo().massUpdate(mu);

            JsonObject json = new JsonObject();
            json.addProperty("action", CoreRedisAction.TAG_UPDATE.toString());
            json.addProperty("fromServer", plugin.getConfig().getString("general.server_name"));
            json.addProperty("tag", tag.getUuid().toString());

            plugin.getRedisPublisher().getMessageQueue().add(new RedisMessage("core", json));

            plugin.getProfileManager().update();
        });
    }

    public void remove(boolean async, Tag tag) {
        plugin.getMongo().deleteDocument(async, "core_tags", tag.getUuid());

        JsonObject json = new JsonObject();
        json.addProperty("action", CoreRedisAction.TAG_DELETE.toString());
        json.addProperty("fromServer", plugin.getConfig().getString("general.server_name"));
        json.addProperty("tag", tag.getUuid().toString());

        plugin.getRedisPublisher().getMessageQueue().add(new RedisMessage("core", json));

        tags.remove(tag.getUuid());

        plugin.getProfileManager().update();
    }
}
