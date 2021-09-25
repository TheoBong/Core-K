package cc.kitpvp.core.commands.tags;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.commands.BaseCommand;
import cc.kitpvp.core.profiles.Profile;
import cc.kitpvp.core.tags.Tag;
import cc.kitpvp.core.utils.Colors;
import cc.kitpvp.core.utils.RandomNoPermission;
import cc.kitpvp.core.web.WebPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveTagCommand extends BaseCommand {

    private final Core core;

    public RemoveTagCommand(Core core, String name) {
        super(name);
        this.core = core;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission("core.tags")) {
            sender.sendMessage(RandomNoPermission.getRandomPermission());
            return;
        }

        if(args.length > 1) {

            Player target = Bukkit.getPlayer(args[0]);
            Profile profile;
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

            Tag tag = core.getTagManager().getTag(args[1]);
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
