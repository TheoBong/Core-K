package cc.kitpvp.core.listeners.player;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.networking.CoreRedisAction;
import cc.kitpvp.core.networking.redis.RedisMessage;
import cc.kitpvp.core.profiles.Profile;
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

    private final Core core;
    public PlayerQuitListener(Core core) {
        this.core = core;
        core.registerListener(this);
    }

    public void onDisconnect(Player player) {
        Profile profile = core.getProfileManager().get(player.getUniqueId());

        if(player.hasPermission("core.staff")) {
            JsonObject json = new JsonObject();
            json.addProperty("action", CoreRedisAction.STAFF_BROADCAST.toString());
            json.addProperty("message", "&7[Staff] &f" + player.getName() + "&a left server &f" + core.getConfig().getString("general.server_name") + "&a.");
            RedisMessage rm = new RedisMessage("core", json);
            core.getRedisPublisher().getMessageQueue().add(rm);
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

        core.getProfileManager().push(true, profile, true);
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
