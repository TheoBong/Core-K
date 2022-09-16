package com.bongbong.core.commands.messaging;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.commands.BaseCommand;
import com.bongbong.core.profiles.Profile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IgnoreCommand extends BaseCommand {

    private final CorePlugin plugin;

    public IgnoreCommand(CorePlugin plugin, String name) {
        super(name);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {

        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(args.length > 0) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                if(offlinePlayer != null) {
                    Profile profile = plugin.getProfileManager().get(player.getUniqueId());
                    if(profile.getIgnored().contains(offlinePlayer.getUniqueId())) {
                        sender.sendMessage(ChatColor.RED + "You are already ignoring this player.");
                    } else {
                        if(player.getUniqueId().equals(offlinePlayer.getUniqueId())) {
                            sender.sendMessage(ChatColor.RED + "Why would you ignore yourself? lmao");
                        }
                        profile.getIgnored().add(offlinePlayer.getUniqueId());
                        sender.sendMessage(ChatColor.GREEN + "You are now ignoring " + ChatColor.WHITE + offlinePlayer.getName() + ChatColor.GREEN + ".");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "The target you specified was not found.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /ignore <player>");
            }
        }
    }
}
