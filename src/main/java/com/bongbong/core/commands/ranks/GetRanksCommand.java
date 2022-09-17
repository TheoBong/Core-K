package com.bongbong.core.commands.ranks;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.commands.BaseCommand;
import com.bongbong.core.profiles.Profile;
import com.bongbong.core.ranks.Rank;
import com.bongbong.core.utils.Colors;
import com.bongbong.core.utils.ThreadUtil;
import com.bongbong.core.utils.WebPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

public class GetRanksCommand extends BaseCommand {

    private final CorePlugin plugin;

    public GetRanksCommand(CorePlugin plugin, String name) {
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
            if(args.length > 0) {
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

                TreeMap<Integer, Rank> ranks = new TreeMap<>();
                for(Rank rank : profile.getAllRanks()) {
                    ranks.put(rank.getWeight(), rank);
                }

                if(!ranks.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("&aRanks &7(" + ranks.size() + "&7)&a: ");
                    List<Rank> list = new LinkedList<>(ranks.descendingMap().values());
                    while(!list.isEmpty()) {
                        final Rank rank = list.get(0);
                        list.remove(rank);
                        sb.append(rank.getColor() + rank.getName());
                        if(list.isEmpty()) {
                            sb.append("&7.");
                        } else {
                            sb.append("&7, ");
                        }
                    }

                    sender.sendMessage(Colors.get(sb.toString()));
                } else {
                    sender.sendMessage(ChatColor.RED + "The target you specified does not have any ranks.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /getranks <target>");
            }
        });
    }

}
