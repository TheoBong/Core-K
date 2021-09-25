package cc.kitpvp.core.commands.ranks;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.commands.BaseCommand;
import cc.kitpvp.core.profiles.Profile;
import cc.kitpvp.core.ranks.Rank;
import cc.kitpvp.core.utils.Colors;
import cc.kitpvp.core.utils.RandomNoPermission;
import cc.kitpvp.core.web.WebPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

public class GetRanksCommand extends BaseCommand {

    private final Core core;

    public GetRanksCommand(Core core, String name) {
        super(name);
        this.core = core;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission("core.ranks")) {
            sender.sendMessage(RandomNoPermission.getRandomPermission());
            return;
        }

        if(args.length > 0) {
            Player target = Bukkit.getPlayer(args[0]);
            Profile profile = null;
            if(target != null) {
                profile = core.getProfileManager().get(target.getUniqueId());
            } else {
                WebPlayer wp = new WebPlayer(args[0]);
                if(wp.isValid()) {
                    profile = core.getProfileManager().find(wp.getUuid(), false);
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
    }
}
