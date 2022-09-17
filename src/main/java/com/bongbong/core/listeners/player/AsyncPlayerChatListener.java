package com.bongbong.core.listeners.player;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.networking.CoreRedisAction;
import com.bongbong.core.networking.redis.RedisMessage;
import com.bongbong.core.profiles.Profile;
import com.bongbong.core.ranks.Rank;
import com.bongbong.core.server.CoreServer;
import com.bongbong.core.utils.Colors;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AsyncPlayerChatListener implements Listener {
    private final CorePlugin plugin;

    public AsyncPlayerChatListener(CorePlugin plugin) {
        this.plugin = plugin;
        plugin.registerListener(this);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Profile profile = plugin.getProfileManager().get(player.getUniqueId());

        Rank rank = profile.getHighestRank();
        String prefix, color, tag;
        if(rank != null) {
            prefix = rank.getPrefix();
            color = rank.getColor();
        } else {
            prefix = null;
            color = "&f";
        }

        tag = null;
        if(profile.getAppliedTag() != null) {
            tag = profile.getAppliedTag().getTag();
        }

        String format = (prefix == null ? "" : prefix + " ") + color + player.getName() + (tag == null ? "" : " " + tag) + "&7: &r" + event.getMessage();
        event.setFormat(Colors.get(format));

        if(profile.getSettings().isStaffChat() && player.hasPermission("core.staff")) {
            event.setCancelled(true);
            JsonObject json = new JsonObject();
            json.addProperty("action", CoreRedisAction.STAFF_BROADCAST.toString());
            json.addProperty("message", "&7[Staff Chat] (" + plugin.getConfig().getString("general.server_name") + ") &r" + format);
            plugin.getRedisPublisher().getMessageQueue().add(new RedisMessage("core", json));
        } else {
            if(!profile.getSettings().isGlobalChat()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot type in global chat because you have it disabled.");
                return;
            }

            CoreServer coreServer = plugin.getCoreServer();
            if (!player.hasPermission("core.staff") && coreServer.isChatMuted()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Global chat is currently muted.");
                return;
            }

            Date date = profile.getCooldowns().get(Profile.Cooldown.CHAT);
            if(!(date == null || date.before(new Date()))) {
                event.setCancelled(true);

                double seconds = (date.getTime() - System.currentTimeMillis()) / 1000.0;
                String dateFormatted = String.format("%.1f seconds", seconds);

                player.sendMessage(ChatColor.RED + "You're on chat cooldown for another: " + dateFormatted);
                return;
            } else if (!player.hasPermission("core.donor")) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.SECOND, 3);
                profile.getCooldowns().put(Profile.Cooldown.CHAT, calendar.getTime());
            }

            List<Player> toRemove = new ArrayList<>();

            for (Player recipient : event.getRecipients()) {
                Profile pr = plugin.getProfileManager().get(recipient.getUniqueId());

                if (pr.getIgnored().contains(player.getUniqueId()) || !pr.getSettings().isGlobalChat()) {
                    toRemove.add(recipient);
                    continue;
                }

                String[] words = event.getMessage().split(" ");
                boolean found = false;

                StringBuilder newMessage = new StringBuilder();

                for (String word : words) {
                    if (recipient.getName().equalsIgnoreCase(word) && !found) {
                        newMessage.append("&a").append("&o").append(word).append("&r").append(" ");
                        found = true;
                    } else {
                        newMessage.append(word).append(" ");
                    }
                }

                if (!found) {
                    continue;
                }

                recipient.playSound(recipient.getLocation(), Sound.LEVEL_UP, 1.0F, 2.0F);

                format = (prefix == null ? "" : prefix + " ") + color + player.getName() + (tag == null ? "" : " " + tag) + "&7: &r" + newMessage;
                recipient.sendMessage(Colors.get(format));

                toRemove.add(recipient);
            }

            for (Player playerToRemove : toRemove) {
                event.getRecipients().remove(playerToRemove);
            }


//            for (Player p : Bukkit.getOnlinePlayers()) {
//                Profile pr = core.getProfileManager().get(p.getUniqueId());
//                if (pr.getIgnored().contains(player.getUniqueId()) || !pr.getSettings().isGlobalChat()) {
//                    event.getRecipients().remove(p);
//                }
//            }
        }
    }
}
