package com.bongbong.core.punishments;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.networking.CoreRedisAction;
import com.bongbong.core.networking.mongo.MongoDeserializedResult;
import com.bongbong.core.networking.mongo.MongoUpdate;
import com.bongbong.core.networking.redis.RedisMessage;
import com.bongbong.core.profiles.Profile;
import com.google.gson.JsonObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PunishmentManager {

    private final CorePlugin plugin;
    private Map<UUID, Punishment> punishments;
    public PunishmentManager(CorePlugin plugin) {
        this.plugin = plugin;
        this.punishments = new HashMap<>();
    }

    public Punishment create(Punishment.Type type, Profile victim, UUID issuer, String reason, Date expires, boolean silent) {
        for(Punishment punishment : victim.getPunishments(type)) {
            if(punishment.isActive()) {
                return null;
            }
        }

        Punishment punishment = new Punishment(plugin, UUID.randomUUID());
        punishment.setType(type);
        punishment.setVictim(victim.getUuid());
        punishment.setIssuer(issuer);
        punishment.setIssueReason(reason);
        punishment.setIssued(new Date());
        punishment.setExpires(expires);
        punishment.setSilentIssue(silent);

        punishments.put(punishment.getUuid(), punishment);
        victim.getPunishments().add(punishment.getUuid());

        push(true, punishment, false);

        if(victim.getPlayer() == null) {
            plugin.getProfileManager().push(true, victim, false);
        }

        return punishment;
    }

    public Punishment getPunishment(UUID uuid) {
        return punishments.get(uuid);
    }

    public void pull(boolean async, UUID uuid, boolean store, MongoDeserializedResult mdr) {
        plugin.getMongo().getDocument(async, "core_punishments", uuid, d -> {
            if(d != null) {
                Punishment punishment = new Punishment(plugin, uuid);
                punishment.importFromDocument(d);

                mdr.call(punishment);
                if(store) {
                    punishments.put(punishment.getUuid(), punishment);
                }
            } else {
                mdr.call(null);
            }
        });
    }

    public void push(boolean async, Punishment punishment, boolean unload) {
        MongoUpdate mu = new MongoUpdate("core_punishments", punishment.getUuid());
        mu.setUpdate(punishment.export());
        plugin.getMongo().massUpdate(async, mu);

        JsonObject json = new JsonObject();
        json.addProperty("action", CoreRedisAction.PUNISHMENT_UPDATE.toString());
        json.addProperty("fromServer", plugin.getConfig().getString("general.server_name"));
        json.addProperty("punishment", punishment.getUuid().toString());

        plugin.getRedisPublisher().getMessageQueue().add(new RedisMessage("core", json));

        if(unload) {
            punishments.remove(punishment.getUuid());
        }
    }
}
