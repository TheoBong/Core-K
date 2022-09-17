package com.bongbong.core.commands.ranks;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.commands.BaseCommand;
import com.bongbong.core.profiles.Profile;
import com.bongbong.core.ranks.Rank;
import com.bongbong.core.utils.ThreadUtil;
import com.bongbong.core.utils.WebPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddRankCommand extends BaseCommand {

    private final CorePlugin plugin;

    public AddRankCommand(CorePlugin plugin, String name) {
        super(name);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission("core.ranks")) {
            sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
            return;
        }

        ThreadUtil.runTask(true, plugin, () -> {
            if(args.length > 1) {
                Rank rank = plugin.getRankManager().getRank(args[1]);
                Player target = Bukkit.getPlayer(args[0]);
                Profile profile = null;
                if(target != null) {
                    profile = plugin.getProfileManager().get(target.getUniqueId());
                } else {
                    WebPlayer wp = new WebPlayer(args[0]);
                    if(wp.isValid()) {
                        profile = plugin.getProfileManager().find(wp.getUuid(), false);
                    } else {
                        sender.sendMessage(ChatColor.RED + "The target you specified does not exist.");
                        return;
                    }
                }

                if (profile == null) {
                    sender.sendMessage(ChatColor.RED + "The target you specified has never joined the server.");
                    return;
                }

                if(rank == null) {
                    sender.sendMessage(ChatColor.RED + "The rank you specified does not exist.");
                    return;
                }

                if(!profile.getRanks().contains(rank.getUuid())) {
                    profile.addRank(rank.getUuid());
                    plugin.getProfileManager().push(false, profile, false);
                    sender.sendMessage(ChatColor.WHITE + profile.getName() + ChatColor.GREEN + " now has the rank " + rank.getColor() + rank.getName() + ChatColor.GREEN + ".");
                } else {
                    sender.sendMessage(ChatColor.RED + "The target you specified already has that rank.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /addrank <target> <rank>");
            }
        });
    }
}
