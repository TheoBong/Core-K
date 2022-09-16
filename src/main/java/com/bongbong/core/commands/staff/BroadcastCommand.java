package com.bongbong.core.commands.staff;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.commands.BaseCommand;
import com.bongbong.core.networking.CoreRedisAction;
import com.bongbong.core.networking.redis.RedisMessage;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BroadcastCommand extends BaseCommand {

    private final CorePlugin plugin;

    public BroadcastCommand(CorePlugin plugin, String name) {
        super(name);
        this.plugin = plugin;
        this.setAliases("staffbroadcast");
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission("core.broadcast")) {
            sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
            return;
        }

        if(args.length > 0) {
            CoreRedisAction cra = CoreRedisAction.BROADCAST;
            if (alias.equalsIgnoreCase("staffbroadcast")) {
                cra = CoreRedisAction.STAFF_BROADCAST;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                sb.append(args[i]);
                if(i + 1 != args.length) {
                    sb.append(" ");
                }
            }

            JsonObject json = new JsonObject();
            json.addProperty("action", cra.toString());
            json.addProperty("message", (cra == CoreRedisAction.STAFF_BROADCAST ? "&7[&4Staff Broadcast&7] &r": "&7[&cBroadcast&7] &r") + sb.toString());
            plugin.getRedisPublisher().getMessageQueue().add(new RedisMessage("core", json));
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /" + alias.toLowerCase() + " <message>");
        }
    }
}
