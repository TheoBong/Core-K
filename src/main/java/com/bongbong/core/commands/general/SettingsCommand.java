package com.bongbong.core.commands.general;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.commands.BaseCommand;
import com.bongbong.core.networking.CoreRedisAction;
import com.bongbong.core.networking.redis.RedisMessage;
import com.bongbong.core.profiles.Profile;
import com.bongbong.core.profiles.Settings;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SettingsCommand extends BaseCommand {

    private final CorePlugin plugin;

    public SettingsCommand(CorePlugin plugin, String name) {
        super(name);
        this.plugin = plugin;
        this.setAliases(
                "toggleglobalchat",
                "tgc",
                "togglepms",
                "tpm",
                "togglestaffmessages",
                "tsm",
                "togglestaffchat",
                "staffchat",
                "sc",
                "clearignored");
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            Profile profile = plugin.getProfileManager().get(player.getUniqueId());
            Settings settings = profile.getSettings();
            switch (alias.toLowerCase()) {
                case "toggleglobalchat":
                case "tgc":
                    settings.setGlobalChat(!settings.isGlobalChat());
                    sender.sendMessage(ChatColor.GREEN + "Global chat is now " + ChatColor.WHITE + (settings.isGlobalChat() ? "enabled" : "disabled") + ChatColor.GREEN + ".");
                    break;
                case "togglepms":
                case "tpm":
                    settings.setPrivateMessages(!settings.isPrivateMessages());
                    sender.sendMessage(ChatColor.GREEN + "Private messages are now " + ChatColor.WHITE + (settings.isPrivateMessages() ? "enabled" : "disabled") + ChatColor.GREEN + ".");
                    break;
                case "togglestaffmessages":
                case "tsm":
                    if (!player.hasPermission("core.staff")) {
                        sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
                        return;
                    }

                    settings.setStaffMessages(!settings.isStaffMessages());
                    sender.sendMessage(ChatColor.GREEN + "Staff messages are now " + ChatColor.WHITE + (settings.isStaffMessages() ? "enabled" : "disabled") + ChatColor.GREEN + ".");
                    break;
                case "staffchat":
                case "sc":
                    if(player.hasPermission("core.staff")) {
                        if (args.length == 1) {
                            String prefix = profile.getHighestRank().getPrefix();
                            String color = profile.getHighestRank().getColor();
                            String tag = null;
                            if (profile.getAppliedTag() != null) {
                                tag = profile.getAppliedTag().toString();
                            }

                            String format = (prefix == null ? "" : prefix + " ") + color + player.getName() + (tag == null ? "" : " " + tag) + "&7: &r" + args[0];

                            JsonObject json = new JsonObject();
                            json.addProperty("action", CoreRedisAction.STAFF_BROADCAST.toString());
                            json.addProperty("message", "&7[Staff Chat] (" + plugin.getConfig().getString("general.server_name") + ") &r" + format);
                            plugin.getRedisPublisher().getMessageQueue().add(new RedisMessage("core", json));
                        } else {
                            settings.setStaffChat(!settings.isStaffChat());
                            sender.sendMessage(ChatColor.GREEN + "Staff chat is now " + ChatColor.WHITE + (settings.isStaffChat() ? "enabled" : "disabled") + ChatColor.GREEN + ".");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
                    }
                    break;
                case "clearignored":
                    profile.getIgnored().clear();
                    sender.sendMessage(ChatColor.GREEN + "Your ignored player list has been cleared.");
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Available commands: /toggleglobalchat, /togglepms, /clearignored.");
            }
        }
    }
}
