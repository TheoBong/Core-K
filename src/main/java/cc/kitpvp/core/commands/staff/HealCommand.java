package cc.kitpvp.core.commands.staff;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.commands.BaseCommand;
import cc.kitpvp.core.utils.RandomNoPermission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HealCommand extends BaseCommand {

    private final Core core;

    public HealCommand(Core core, String name) {
        super(name);
        this.core = core;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission("core.heal")) {
            sender.sendMessage(RandomNoPermission.getRandomPermission());
            return;
        }

        if(sender instanceof Player) {
            Player player = (Player) sender;
            Player target;

            if(args.length > 0) {
                target = Bukkit.getPlayer(args[0]);
                if(target != null) {
                    player.sendMessage(ChatColor.GREEN + "You healed " + ChatColor.WHITE + target.getName() + ChatColor.GREEN + ".");
                } else {
                    player.sendMessage(ChatColor.RED + "The target you specified is not on this server.");
                    return;
                }
            } else {
                target = player;
            }

            target.setHealth(target.getMaxHealth());
            target.sendMessage(ChatColor.GREEN + "You have been healed.");
        }
    }
}
