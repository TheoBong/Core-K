package com.bongbong.core.profiles;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.networking.CoreRedisAction;
import com.bongbong.core.networking.mongo.MongoDeserializedResult;
import com.bongbong.core.networking.mongo.MongoUpdate;
import com.bongbong.core.networking.redis.RedisMessage;
import com.bongbong.core.ranks.Rank;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class ProfileManager {

    private final CorePlugin plugin;
    private @Getter Map<UUID, Profile> profiles;
    public ProfileManager(CorePlugin plugin) {
        this.plugin = plugin;
        this.profiles = new HashMap<>();
    }

    public Profile createProfile(Player player) {
        Profile profile = new Profile(plugin, player);
        profiles.put(profile.getUuid(), profile);

        Rank rank = plugin.getRankManager().getDefaultRank();
        if(rank != null) {
            profile.addRank(rank.getUuid());
        } else {
            plugin.getLogger().warning("No default rank! Please create a default rank.");
        }

        push(true, profile, false);
        return profile;
    }

    public Profile createProfile(UUID uuid) {
        Profile profile = new Profile(plugin, uuid);
        profiles.put(profile.getUuid(), profile);

        Rank rank = plugin.getRankManager().getDefaultRank();
        if(rank != null) {
            profile.addRank(rank.getUuid());
        } else {
            plugin.getLogger().warning("No default rank! Please create a default rank.");
        }

        push(true, profile, false);
        return profile;
    }

    public Profile find(UUID uuid, boolean store) {
        final Profile[] profile = {profiles.get(uuid)};
        if(profile[0] == null) {
            pull(false, uuid, store, mdr -> {
                if(mdr instanceof Profile) {
                    profile[0] = (Profile) mdr;
                }
            });
        }
        return profile[0];
    }

    public Profile get(UUID uuid) {
        return profiles.get(uuid);
    }

    public void pull(boolean async, UUID uuid, boolean store, MongoDeserializedResult mdr) {
        plugin.getMongo().getDocument(async, "core_profiles", uuid, document -> {
            if(document != null) {
                Profile profile = new Profile(plugin, uuid);
                profile.importFromDocument(document);

                for(UUID u : profile.getPunishments()) {
                    plugin.getPunishmentManager().pull(false, u, true, obj -> {});
                }

                mdr.call(profile);
                if(store) {
                    profiles.put(profile.getUuid(), profile);
                }
            } else {
                mdr.call(null);
            }
        });
    }

    public void push(boolean async, Profile profile, boolean unload) {
        MongoUpdate mu = new MongoUpdate("core_profiles", profile.getUuid());
        mu.setUpdate(profile.export());
        plugin.getMongo().massUpdate(async, mu);

        JsonObject json = new JsonObject();
        json.addProperty("action", CoreRedisAction.PROFILE_UPDATE.toString());
        json.addProperty("fromServer", plugin.getConfig().getString("general.server_name"));
        json.addProperty("uuid", profile.getUuid().toString());

        plugin.getRedisPublisher().getMessageQueue().add(new RedisMessage("core", json));

        if(unload) {
            profiles.remove(profile.getUuid());
        }
    }

    public void update() {
        for(Profile profile : profiles.values()) {
            profile.update();
        }
    }

    public void shutdown() {
        HashSet<Profile> toRemove = new HashSet<>();

        for(Profile profile : profiles.values()) {
            Player player = profile.getPlayer();
            if(player != null && player.isOnline()) {
                toRemove.add(profile);
            }
        }

        toRemove.forEach(profile -> push(false, profile, true));
    }
}
