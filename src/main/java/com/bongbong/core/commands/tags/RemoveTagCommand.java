package com.bongbong.core.commands.tags;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.commands.BaseCommand;
import com.bongbong.core.profiles.Profile;
import com.bongbong.core.tags.Tag;
import com.bongbong.core.utils.Colors;
import com.bongbong.core.web.WebPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveTagCommand extends BaseCommand {

    private final CorePlugin plugin;

    public RemoveTagCommand(CorePlugin plugin, String name) {
        super(name);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission("core.tags")) {
            sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
            return;
        }

        if(args.length > 1) {

            Player target = Bukkit.getPlayer(args[0]);
            Profile profile;
            if(target != null) {
                profile = plugin.getProfileManager().get(target.getUniqueId());
            } else {
                WebPlayer wp = new WebPlayer(args[0]);
                if(wp.isValid()) {
                    profile = plugin.getProfileManager().find(wp.getUuid(), false);
                } else {
                    sender.sendMessage(ChatColor.RED + "The target you specified does not exist.");
                    return;
                }
            }

            if (profile == null) {
                sender.sendMessage(ChatColor.RED + "The target you specified has never joined the server.");
                return;
            }

            Tag tag = plugin.getTagManager().getTag(args[1]);
            if(tag != null) {
                if(profile.getTags().contains(tag.getUuid())) {
                    profile.getTags().remove(tag.getUuid());
                    sender.sendMessage(ChatColor.WHITE + profile.getName() + ChatColor.GREEN + " no longer owns the tag " + Colors.get(tag.getColor() + tag.getDisplayName()) + ChatColor.GREEN + ".");
                } else {
                    sender.sendMessage(ChatColor.RED + "The target you specified doesn't own that tag.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "The tag you specified does not exist.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /addtag <target> <tag>");
        }
    }
}