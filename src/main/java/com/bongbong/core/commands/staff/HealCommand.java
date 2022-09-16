package com.bongbong.core.commands.staff;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.commands.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HealCommand extends BaseCommand {

    private final CorePlugin plugin;

    public HealCommand(CorePlugin plugin, String name) {
        super(name);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission("core.heal")) {
            sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
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
