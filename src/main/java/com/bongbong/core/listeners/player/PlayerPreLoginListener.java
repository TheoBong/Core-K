package com.bongbong.core.listeners.player;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.profiles.Profile;
import com.bongbong.core.punishments.Punishment;
import com.bongbong.core.server.CoreServer;
import com.bongbong.core.utils.Colors;
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

            Punishment blacklist = profile.getActivePunishment(Punishment.Type.BLACKLIST);
            Punishment ban = profile.getActivePunishment(Punishment.Type.BAN);
            if (blacklist != null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                        Colors.get("&4You are blacklisted!\n&fReason: " + blacklist.getIssueReason()));
                profile.addIp(event.getAddress().getHostAddress());
                plugin.getProfileManager().push(true, profile, true);
            }

            if (ban != null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                        Colors.get("&cYou are banned (Expiry: " + ban.expiry() + ")!\n&fReason: " + ban.getIssueReason()));
                profile.addIp(event.getAddress().getHostAddress());
                plugin.getProfileManager().push(true, profile, true);
            }

            if (!coreServer.getWhitelistMode().allowLogin(plugin, profile)) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, coreServer.getWhitelistMode().getMessage());
                profile.addIp(event.getAddress().getHostAddress());
                plugin.getProfileManager().push(true, profile, true);
                return;
            }

            if (ban == null && blacklist == null) {
                profile.addIp(event.getAddress().getHostAddress());
                event.allow();
            }
        }
    }
}
