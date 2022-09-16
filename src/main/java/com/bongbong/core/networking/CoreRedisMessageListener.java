package com.bongbong.core.networking;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.networking.redis.RedisMessage;
import com.bongbong.core.networking.redis.RedisMessageListener;
import com.bongbong.core.profiles.Profile;
import com.bongbong.core.utils.ClickableMessage;
import com.bongbong.core.utils.Colors;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CoreRedisMessageListener implements RedisMessageListener {

    private final CorePlugin plugin;
    private String serverName;
    public CoreRedisMessageListener(CorePlugin plugin) {
        this.plugin = plugin;
        plugin.getRedisSubscriber().getListeners().add(this);

        this.serverName = plugin.getConfig().getString("general.server_name");
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
                                plugin.getProfileManager().pull(true, uuid, true, obj -> {
                                });
                            } else {
                                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                    Player p = Bukkit.getPlayer(uuid);
                                    if (p != null && p.isOnline()) {
                                        plugin.getProfileManager().pull(true, uuid, true, obj -> {
                                        });
                                    }
                                }, 10);
                            }
                        }
                        break;
                    case RANK_UPDATE:
                        if(!thisServer) {
                            plugin.getRankManager().pull(true, UUID.fromString(json.get("rank").getAsString()), obj -> { });
                        }
                        break;
                    case RANK_DELETE:
                        if(!thisServer) {
                            plugin.getRankManager().getRanks().remove(UUID.fromString(json.get("rank").getAsString()));
                        }
                        break;
                    case TAG_UPDATE:
                        if(!thisServer) {
                            plugin.getTagManager().pull(true, UUID.fromString(json.get("tag").getAsString()), obj -> {});
                        }
                        break;
                    case TAG_DELETE:
                        if(!thisServer) {
                            plugin.getTagManager().getTags().remove(UUID.fromString(json.get("tag").getAsString()));
                        }
                }
            } else {
                switch(action) {
                    case BROADCAST:
                        Bukkit.broadcastMessage(Colors.get(json.get("message").getAsString()));
                        break;
                    case PUNISHMENT:
                        for(Profile profile : plugin.getProfileManager().getProfiles().values()) {
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
                        for(Profile profile : plugin.getProfileManager().getProfiles().values()) {
                            Player player = profile.getPlayer();
                            if(player != null && player.isOnline() && player.hasPermission("core.staff") && profile.getSettings().isStaffMessages()) {
                                final ClickableMessage punish = new ClickableMessage(Colors.get(json.get("message").getAsString()))
                                        .hover(Colors.get(json.get("hover").getAsString()));
                                punish.sendToPlayer(player);
                            }
                        }
                        break;
                    case STAFF_BROADCAST:
                        for(Profile profile : plugin.getProfileManager().getProfiles().values()) {
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
        plugin.getRedisSubscriber().getListeners().remove(this);
    }
}
