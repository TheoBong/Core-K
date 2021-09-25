package cc.kitpvp.core.listeners.player;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.profiles.Profile;
import cc.kitpvp.core.punishments.Punishment;
import cc.kitpvp.core.server.CoreServer;
import cc.kitpvp.core.utils.Colors;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;

public class PlayerPreLoginListener implements Listener {

    private final Core core;
    public PlayerPreLoginListener(Core core) {
        this.core = core;
        core.registerListener(this);
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        CoreServer coreServer = core.getCoreServer();

        if (core.getProfileManager().getProfiles().containsKey(uuid)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Your profile is already loaded, please relog! (Core)");

            Profile profile = core.getProfileManager().get(uuid);

            if (profile == null) {
                core.getProfileManager().getProfiles().remove(uuid);
            } else {
                core.getProfileManager().push(false, profile, true);
            }
        } else {
            Profile profile = core.getProfileManager().find(uuid, true);
            if (profile == null) {
                profile = core.getProfileManager().createProfile(uuid);
            }

            Punishment blacklist = profile.getActivePunishment(Punishment.Type.BLACKLIST);
            Punishment ban = profile.getActivePunishment(Punishment.Type.BAN);
            if (blacklist != null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                        Colors.get("&4You are blacklisted!\n&fReason: " + blacklist.getIssueReason()));
                profile.addIp(event.getAddress().getHostAddress());
                core.getProfileManager().push(true, profile, true);
            }

            if (ban != null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                        Colors.get("&cYou are banned (Expiry: " + ban.expiry() + ")!\n&fReason: " + ban.getIssueReason()));
                profile.addIp(event.getAddress().getHostAddress());
                core.getProfileManager().push(true, profile, true);
            }

            if (!coreServer.getWhitelistMode().allowLogin(profile)) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, coreServer.getWhitelistMode().getMessage());
                profile.addIp(event.getAddress().getHostAddress());
                core.getProfileManager().push(true, profile, true);
                return;
            }

            if (ban == null && blacklist == null) {
                profile.addIp(event.getAddress().getHostAddress());
                event.allow();
            }
        }
    }
}
