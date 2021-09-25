package cc.kitpvp.core.networking;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.networking.redis.RedisMessage;
import cc.kitpvp.core.networking.redis.RedisMessageListener;
import cc.kitpvp.core.profiles.Profile;
import cc.kitpvp.core.utils.ClickableMessage;
import cc.kitpvp.core.utils.Colors;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CoreRedisMessageListener implements RedisMessageListener {

    private final Core core;
    private String serverName;
    public CoreRedisMessageListener(Core core) {
        this.core = core;
        core.getRedisSubscriber().getListeners().add(this);

        this.serverName = core.getConfig().getString("general.server_name");
    }

    @Override
    public void onReceive(RedisMessage redisMessage) {
        JsonObject json = redisMessage.getElements();
        if(redisMessage.getInternalChannel().equals("core")) {
            CoreRedisAction action = CoreRedisAction.valueOf(json.get("action").getAsString());
            String fromServer = json.get("fromServer") == null ? null : json.get("fromServer").getAsString();
            if(fromServer != null) {
                boolean thisServer = fromServer.equals(serverName);
                switch(action) {
                    case PROFILE_UPDATE:
                        if(!thisServer) {
                            UUID uuid = UUID.fromString(json.get("uuid").getAsString());
                            Player player = Bukkit.getPlayer(uuid);
                            if (player != null && player.isOnline()) {
                                core.getProfileManager().pull(true, uuid, true, obj -> {
                                });
                            } else {
                                Bukkit.getScheduler().runTaskLater(core, () -> {
                                    Player p = Bukkit.getPlayer(uuid);
                                    if (p != null && p.isOnline()) {
                                        core.getProfileManager().pull(true, uuid, true, obj -> {
                                        });
                                    }
                                }, 10);
                            }
                        }
                        break;
                    case RANK_UPDATE:
                        if(!thisServer) {
                            core.getRankManager().pull(true, UUID.fromString(json.get("rank").getAsString()), obj -> { });
                        }
                        break;
                    case RANK_DELETE:
                        if(!thisServer) {
                            core.getRankManager().getRanks().remove(UUID.fromString(json.get("rank").getAsString()));
                        }
                        break;
                    case TAG_UPDATE:
                        if(!thisServer) {
                            core.getTagManager().pull(true, UUID.fromString(json.get("tag").getAsString()), obj -> {});
                        }
                        break;
                    case TAG_DELETE:
                        if(!thisServer) {
                            core.getTagManager().getTags().remove(UUID.fromString(json.get("tag").getAsString()));
                        }
                }
            } else {
                switch(action) {
                    case BROADCAST:
                        Bukkit.broadcastMessage(Colors.get(json.get("message").getAsString()));
                        break;
                    case PUNISHMENT:
                        for(Profile profile : core.getProfileManager().getProfiles().values()) {
                            Player player = profile.getPlayer();
                            if(player != null && player.isOnline() && player.hasPermission("core.staff") && profile.getSettings().isStaffMessages()) {
                                final ClickableMessage punish = new ClickableMessage(Colors.get(json.get("message").getAsString()))
                                        .hover(Colors.get(json.get("hover").getAsString()));
                                punish.sendToPlayer(player);
                            } else if (player != null) {
                                player.sendMessage(Colors.get(json.get("message").getAsString()));
                            }
                        }
                        break;
                    case PUNISHMENT_SILENT:
                        for(Profile profile : core.getProfileManager().getProfiles().values()) {
                            Player player = profile.getPlayer();
                            if(player != null && player.isOnline() && player.hasPermission("core.staff") && profile.getSettings().isStaffMessages()) {
                                final ClickableMessage punish = new ClickableMessage(Colors.get(json.get("message").getAsString()))
                                        .hover(Colors.get(json.get("hover").getAsString()));
                                punish.sendToPlayer(player);
                            }
                        }
                        break;
                    case STAFF_BROADCAST:
                        for(Profile profile : core.getProfileManager().getProfiles().values()) {
                            Player player = profile.getPlayer();
                            if(player != null && player.isOnline() && player.hasPermission("core.staff") && profile.getSettings().isStaffMessages()) {
                                player.sendMessage(Colors.get(json.get("message").getAsString()));
                            }
                        }
                        break;
                }
            }
        }
    }

    public void close() {
        core.getRedisSubscriber().getListeners().remove(this);
    }
}
