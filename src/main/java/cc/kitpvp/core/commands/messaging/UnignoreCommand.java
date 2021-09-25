package cc.kitpvp.core.commands.messaging;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.commands.BaseCommand;
import cc.kitpvp.core.profiles.Profile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnignoreCommand extends BaseCommand {

    private final Core core;

    public UnignoreCommand(Core core, String name) {
        super(name);
        this.core = core;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {

        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(args.length > 0) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                if(offlinePlayer != null) {
                    Profile profile = core.getProfileManager().get(player.getUniqueId());
                    if(!profile.getIgnored().contains(offlinePlayer.getUniqueId())) {
                        sender.sendMessage(ChatColor.RED + "You are not ignoring this player.");
                    } else {
                        profile.getIgnored().remove(offlinePlayer.getUniqueId());
                        sender.sendMessage(ChatColor.GREEN + "You are no longer ignoring " + ChatColor.WHITE + offlinePlayer.getName() + ChatColor.GREEN + ".");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "The target you specified was not found.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /unignore <player>");
            }
        }
    }
}
