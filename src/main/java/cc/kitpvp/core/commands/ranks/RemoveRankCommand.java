package cc.kitpvp.core.commands.ranks;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.commands.BaseCommand;
import cc.kitpvp.core.profiles.Profile;
import cc.kitpvp.core.ranks.Rank;
import cc.kitpvp.core.utils.RandomNoPermission;
import cc.kitpvp.core.web.WebPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveRankCommand extends BaseCommand {

    private final Core core;

    public RemoveRankCommand(Core core, String name) {
        super(name);
        this.core = core;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission("core.ranks")) {
            sender.sendMessage(RandomNoPermission.getRandomPermission());
            return;
        }

        if(args.length > 1) {
            Rank rank = core.getRankManager().getRank(args[1]);
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

            if(rank == null) {
                sender.sendMessage(ChatColor.RED + "The rank you specified does not exist.");
                return;
            }

            if(profile.getRanks().contains(rank.getUuid())) {
                profile.removeRank(rank.getUuid());
                core.getProfileManager().push(true, profile, false);
                sender.sendMessage(ChatColor.WHITE + profile.getName() + ChatColor.GREEN + " no longer has the rank " + rank.getColor() + rank.getName() + ChatColor.GREEN + ".");
            } else {
                sender.sendMessage(ChatColor.RED + "The target you specified doesn't have that rank.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /removerank <target> <rank>");
        }
    }
}
