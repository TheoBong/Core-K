package com.bongbong.core.listeners.player;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.networking.CoreRedisAction;
import com.bongbong.core.networking.redis.RedisMessage;
import com.bongbong.core.profiles.Profile;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerQuitListener implements Listener {

    private final CorePlugin plugin;
    public PlayerQuitListener(CorePlugin plugin) {
        this.plugin = plugin;
        plugin.registerListener(this);
    }

    public void onDisconnect(Player player) {
        Profile profile = plugin.getProfileManager().get(player.getUniqueId());

        if(player.hasPermission("core.staff")) {
            JsonObject json = new JsonObject();
            json.addProperty("action", CoreRedisAction.STAFF_BROADCAST.toString());
            json.addProperty("message", "&7[Staff] &f" + player.getName() + "&a left server &f" + plugin.getConfig().getString("general.server_name") + "&a.");
            RedisMessage rm = new RedisMessage("core", json);
            plugin.getRedisPublisher().getMessageQueue().add(rm);
        }

        if (profile == null) {
            return;
        }

        if (profile.getFriends() != null && !profile.getFriends().isEmpty()) {
            for (String friendUUID : profile.getFriends().keySet()) {
                Player friend;
                try {
                    friend = Bukkit.getPlayer(UUID.fromString(friendUUID));
                } catch (NullPointerException e) { continue; }

                if (friend == null) {
                    continue;
                }

                if (friend.isOnline()) {
                    friend.sendMessage(ChatColor.translateAlternateColorCodes('&',"&a[Friends] &e" + player.getName() + " just left the server!"));
                }
            }
        }

        plugin.getProfileManager().push(true, profile, true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        onDisconnect(event.getPlayer());
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        onDisconnect(event.getPlayer());
        event.setLeaveMessage(null);
    }
}
