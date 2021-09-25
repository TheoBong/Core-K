package cc.kitpvp.core.profiles;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.networking.CoreRedisAction;
import cc.kitpvp.core.networking.mongo.MongoDeserializedResult;
import cc.kitpvp.core.networking.mongo.MongoUpdate;
import cc.kitpvp.core.networking.redis.RedisMessage;
import cc.kitpvp.core.ranks.Rank;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class ProfileManager {

    private final Core core;
    private @Getter Map<UUID, Profile> profiles;
    public ProfileManager(Core core) {
        this.core = core;
        this.profiles = new HashMap<>();
    }

    public Profile createProfile(Player player) {
        Profile profile = new Profile(player);
        profiles.put(profile.getUuid(), profile);

        Rank rank = core.getRankManager().getDefaultRank();
        if(rank != null) {
            profile.addRank(rank.getUuid());
        } else {
            core.getLogger().warning("No default rank! Please create a default rank.");
        }

        push(true, profile, false);
        return profile;
    }

    public Profile createProfile(UUID uuid) {
        Profile profile = new Profile(uuid);
        profiles.put(profile.getUuid(), profile);

        Rank rank = core.getRankManager().getDefaultRank();
        if(rank != null) {
            profile.addRank(rank.getUuid());
        } else {
            core.getLogger().warning("No default rank! Please create a default rank.");
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
        core.getMongo().getDocument(async, "core_profiles", uuid, document -> {
            if(document != null) {
                Profile profile = new Profile(uuid);
                profile.importFromDocument(document);

                for(UUID u : profile.getPunishments()) {
                    core.getPunishmentManager().pull(false, u, true, obj -> {});
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
        core.getMongo().massUpdate(async, mu);

        JsonObject json = new JsonObject();
        json.addProperty("action", CoreRedisAction.PROFILE_UPDATE.toString());
        json.addProperty("fromServer", core.getConfig().getString("general.server_name"));
        json.addProperty("uuid", profile.getUuid().toString());

        core.getRedisPublisher().getMessageQueue().add(new RedisMessage("core", json));

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
