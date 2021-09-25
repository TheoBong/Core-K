package cc.kitpvp.core.punishments;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.networking.CoreRedisAction;
import cc.kitpvp.core.networking.mongo.MongoDeserializedResult;
import cc.kitpvp.core.networking.mongo.MongoUpdate;
import cc.kitpvp.core.networking.redis.RedisMessage;
import cc.kitpvp.core.profiles.Profile;
import com.google.gson.JsonObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PunishmentManager {

    private final Core core;
    private Map<UUID, Punishment> punishments;
    public PunishmentManager(Core core) {
        this.core = core;
        this.punishments = new HashMap<>();
    }

    public Punishment create(Punishment.Type type, Profile victim, UUID issuer, String reason, Date expires, boolean silent) {
        for(Punishment punishment : victim.getPunishments(type)) {
            if(punishment.isActive()) {
                return null;
            }
        }

        Punishment punishment = new Punishment(UUID.randomUUID());
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
            core.getProfileManager().push(true, victim, false);
        }

        return punishment;
    }

    public Punishment getPunishment(UUID uuid) {
        return punishments.get(uuid);
    }

    public void pull(boolean async, UUID uuid, boolean store, MongoDeserializedResult mdr) {
        core.getMongo().getDocument(async, "core_punishments", uuid, d -> {
            if(d != null) {
                Punishment punishment = new Punishment(uuid);
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
        core.getMongo().massUpdate(async, mu);

        JsonObject json = new JsonObject();
        json.addProperty("action", CoreRedisAction.PUNISHMENT_UPDATE.toString());
        json.addProperty("fromServer", core.getConfig().getString("general.server_name"));
        json.addProperty("punishment", punishment.getUuid().toString());

        core.getRedisPublisher().getMessageQueue().add(new RedisMessage("core", json));

        if(unload) {
            punishments.remove(punishment.getUuid());
        }
    }
}
