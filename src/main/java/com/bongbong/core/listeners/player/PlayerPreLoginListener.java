package com.bongbong.core.listeners.player;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.profiles.Profile;
import com.bongbong.core.server.CoreServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;

public class PlayerPreLoginListener implements Listener {

    private final CorePlugin plugin;
    public PlayerPreLoginListener(CorePlugin plugin) {
        this.plugin = plugin;
        plugin.registerListener(this);
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        CoreServer coreServer = plugin.getCoreServer();

        if (plugin.getProfileManager().getProfiles().containsKey(uuid)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Your profile is already loaded, please relog! (Core)");

            Profile profile = plugin.getProfileManager().get(uuid);

            if (profile == null) {
                plugin.getProfileManager().getProfiles().remove(uuid);
            } else {
                plugin.getProfileManager().push(false, profile, true);
            }
        } else {
            Profile profile = plugin.getProfileManager().find(uuid, true);
            if (profile == null) {
                profile = plugin.getProfileManager().createProfile(uuid);
            }

            if (!coreServer.getWhitelistMode().allowLogin(plugin, profile)) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, coreServer.getWhitelistMode().getMessage());
                profile.addIp(event.getAddress().getHostAddress());
                plugin.getProfileManager().push(true, profile, true);
                return;
            }
        }
    }
}
