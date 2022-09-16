package com.bongbong.core.commands.staff;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.commands.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SudoCommand extends BaseCommand {

    private final CorePlugin plugin;

    public SudoCommand(CorePlugin plugin, String name) {
        super(name);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission("core.sudo")) {
            sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
            return;
        }

        if(args.length > 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if(target != null) {
                StringBuilder sb = new StringBuilder();
                for(int i = 1; i < args.length; i++) {
                    sb.append(args[i]);
                    if(i + 1 != args.length) {
                        sb.append(" ");
                    }
                }

                String request = sb.toString();
                if(request.toLowerCase().startsWith("c:")) {
                    String chat = request.substring(2);
                    if(chat.isEmpty()) {
                        sender.sendMessage(ChatColor.RED + "You cannot send a blank message.");
                    } else {
                        sender.sendMessage(ChatColor.GREEN + "You made " + ChatColor.WHITE + target.getName() + ChatColor.GREEN + " say: " + ChatColor.WHITE + chat);
                        target.chat(chat);
                    }
                } else {
                    sender.sendMessage(ChatColor.GREEN + "You made " + ChatColor.WHITE + target.getName() + ChatColor.GREEN + " perform command: " + ChatColor.WHITE + request);
                    target.performCommand(request);
                }
            } else {
                sender.sendMessage(ChatColor.RED + "The target you specified is not on this server.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /sudo <player> <action>");
        }
    }
}
