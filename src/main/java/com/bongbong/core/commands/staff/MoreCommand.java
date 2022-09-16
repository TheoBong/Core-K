package com.bongbong.core.commands.staff;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.commands.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MoreCommand extends BaseCommand {

    private final CorePlugin plugin;

    public MoreCommand(CorePlugin plugin, String name) {
        super(name);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission("core.more")) {
            sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
            return;
        }

        if(sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack item = player.getItemInHand();
            if(item != null) {
                item.setAmount(64);
                player.sendMessage(ChatColor.GREEN + "There you go.");
            } else {
                player.sendMessage(ChatColor.RED + "Want more nothing? Idiot.");
            }
        }
    }
}
