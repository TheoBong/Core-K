package cc.kitpvp.core.listeners.player;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.networking.CoreRedisAction;
import cc.kitpvp.core.networking.redis.RedisMessage;
import cc.kitpvp.core.profiles.CorePermissibleBase;
import cc.kitpvp.core.profiles.PermissionInjector;
import cc.kitpvp.core.profiles.Profile;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final Core core;

    public PlayerJoinListener(Core core) {
        this.core = core;
        core.registerListener(this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Profile profile = core.getProfileManager().get(player.getUniqueId());

        if(profile == null) {
            player.kickPlayer(ChatColor.RED + "Your profile did not load properly, please relog.");
            return;
        }

        profile.setName(player.getName());
        profile.addIp(player.getAddress().getAddress().getHostAddress());

        PermissionInjector.inject(player, new CorePermissibleBase(player));
        profile.update();

        if(player.hasPermission("core.staff")) {
            JsonObject json = new JsonObject();
            json.addProperty("action", CoreRedisAction.STAFF_BROADCAST.toString());
            json.addProperty("message", "&7[Staff] &f" + player.getName() + "&a joined server &f" + core.getConfig().getString("general.server_name") + "&a.");
            RedisMessage rm = new RedisMessage("core", json);
            core.getRedisPublisher().getMessageQueue().add(rm);
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
                    friend.sendMessage(ChatColor.translateAlternateColorCodes('&',"&a[Friends] &e" + player.getName() + " just joined the server!"));
                }
            }
        }

        event.setJoinMessage(null);


//        Below code causes severe lag due to making HTTP API requests on the main thread! Very bad!
//        if(core.getConfig().getBoolean("general.namemc_check")) {
//            try {
//                InputStream input = new URL(core.getConfig().getString("general.namemc_api".replace("<uuid>", player.getUniqueId().toString()))).openStream();
//                BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
//
//                StringBuilder sb = new StringBuilder();
//                int cp;
//                while ((cp = reader.read()) != -1) {
//                    sb.append((char) cp);
//                }
//
//                profile.setNameMc(Boolean.parseBoolean(sb.toString()));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }
}
