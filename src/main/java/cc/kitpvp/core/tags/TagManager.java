package cc.kitpvp.core.tags;

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TagManager {

    private final Core core;
    private @Getter Map<UUID, Tag> tags;
    public TagManager(Core core) {
        this.core = core;
        this.tags = new HashMap<>();

        Mongo mongo = core.getMongo();
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
        core.getMongo().getDocument(async, "core_tags", uuid, document -> {
            if(document != null) {
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                Tag tag = gson.fromJson(document.getString("elements"), Tag.class);
                tags.put(uuid, tag);
                mdr.call(tag);
            } else {
                mdr.call(null);
            }
        });

        core.getProfileManager().update();
    }

    public void push(boolean async, Tag tag) {
        MongoUpdate mu = new MongoUpdate("core_tags", tag.getUuid());
        mu.put("elements", tag.serialize());
        core.getMongo().massUpdate(async, mu);

        JsonObject json = new JsonObject();
        json.addProperty("action", CoreRedisAction.TAG_UPDATE.toString());
        json.addProperty("fromServer", core.getConfig().getString("general.server_name"));
        json.addProperty("tag", tag.getUuid().toString());

        core.getRedisPublisher().getMessageQueue().add(new RedisMessage("core", json));

        core.getProfileManager().update();
    }

    public void remove(boolean async, Tag tag) {
        core.getMongo().deleteDocument(async, "core_tags", tag.getUuid());

        JsonObject json = new JsonObject();
        json.addProperty("action", CoreRedisAction.TAG_DELETE.toString());
        json.addProperty("fromServer", core.getConfig().getString("general.server_name"));
        json.addProperty("tag", tag.getUuid().toString());

        core.getRedisPublisher().getMessageQueue().add(new RedisMessage("core", json));

        tags.remove(tag.getUuid());

        core.getProfileManager().update();
    }
}
