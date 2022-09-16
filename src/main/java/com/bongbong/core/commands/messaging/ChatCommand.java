package com.bongbong.core.commands.messaging;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.commands.BaseCommand;
import com.bongbong.core.server.CoreServer;
import com.bongbong.core.utils.Colors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class ChatCommand extends BaseCommand {
    private static final String BLANK_MESSAGE = String.join("", Collections.nCopies(300, "§8 §8 §1 §3 §3 §7 §8 §r\n"));

    private final CorePlugin plugin;

    public ChatCommand(CorePlugin plugin, String name) {
        super(name, "&a&lChat Help &7(Page <page_number>/<pages>)");
        this.plugin = plugin;

        this.setAliases(
                "clearchat",
                "cc",
                "mutechat",
                "unmutechat");

        getCommandHelper()
                .addEntry("&e/chat clear &7- &fClear chat (doesn't clear for staff).")
                .addEntry("&e/chat mute &7- &fToggle chat mute.");
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission("core.staff")) {
            sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
            return;
        }

        String getSender = sender instanceof Player ? ((Player) sender).getPlayer().getName() : Colors.get("&cConsole");

        CoreServer coreServer;

        if(args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "clear":
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        if (!player.hasPermission("core.staff")) {
                            player.sendMessage(BLANK_MESSAGE);
                            player.sendMessage(Colors.get("&aChat has been cleared by " + getSender));
                        } else {
                            player.sendMessage(Colors.get("&aChat has been cleared by " + getSender + " (You bypassed because you are staff)"));
                        }
                    }
                    break;
                case "mute":
                    coreServer = plugin.getCoreServer();

                    coreServer.setChatMuted(!coreServer.isChatMuted());
                    String muted = (coreServer.isChatMuted() ? "muted" : "unmuted");

                    sender.sendMessage(Colors.get("&aChat is now " + muted + "."));
                    Bukkit.broadcastMessage(Colors.get("&cGlobal chat has been " + muted + " by " + getSender));
                    break;
            }
        } else {
            switch(alias.toLowerCase()) {
            case "clearchat":
            case "cc":
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (!player.hasPermission("core.staff")) {
                        player.sendMessage(BLANK_MESSAGE);
                        player.sendMessage(Colors.get("&aChat has been cleared by " + getSender));
                    } else {
                        player.sendMessage(Colors.get("&aChat has been cleared by " + getSender + " (You bypassed because you are staff)"));
                    }
                }
                break;
            case "mutechat":
                coreServer = plugin.getCoreServer();

                coreServer.setChatMuted(!coreServer.isChatMuted());
                String muted = (coreServer.isChatMuted() ? "muted" : "unmuted");

                sender.sendMessage(Colors.get("&aChat is now " + muted + "."));
                Bukkit.broadcastMessage(Colors.get("&cGlobal chat has been " + muted + " by " + getSender));
                break;
            case "unmutechat":
                coreServer = plugin.getCoreServer();

                if (!coreServer.isChatMuted()) {
                    sender.sendMessage(Colors.get("&cChat is not currently muted!"));
                    return;
                }

                coreServer.setChatMuted(false);
                sender.sendMessage(Colors.get("&aChat is now unmuted."));
                Bukkit.broadcastMessage(Colors.get("&cGlobal chat has been unmuted by " + getSender));
                break;
            default:
                sender.sendMessage(getCommandHelper().getMessage(1));
                break;
            }
        }
    }
}
