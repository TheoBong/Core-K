package cc.kitpvp.core.commands.messaging;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.commands.BaseCommand;
import cc.kitpvp.core.profiles.Profile;
import cc.kitpvp.core.punishments.Punishment;
import cc.kitpvp.core.utils.Colors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageCommand extends BaseCommand {

    private final Core core;

    public MessageCommand(Core core, String name) {
        super(name);
        this.core = core;
        this.setAliases(
                "message",
                "msg",
                "m",
                "pm",
                "reply",
                "r");
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {

        if(sender instanceof Player) {
            Player player = (Player) sender;
            Profile profile = core.getProfileManager().get(player.getUniqueId());
            Player target;
            String message;

            if (profile.getActivePunishment(Punishment.Type.MUTE) != null) {
                sender.sendMessage(ChatColor.RED + "You cannot message anyone as you are currently muted.");
                return;
            }

            if (!profile.getSettings().isPrivateMessages()) {
                sender.sendMessage(ChatColor.RED + "You cannot message anyone because you have private messages disabled.");
                return;
            }

            int msgStart = 0;
            switch (alias.toLowerCase()) {
                case "message":
                case "msg":
                case "m":
                case "pm":
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Usage: /msg <player> <message>");
                        return;
                    }

                    target = Bukkit.getPlayer(args[0]);
                    if (target != null && target.isOnline()) {
                        if (target.getUniqueId().equals(player.getUniqueId())) {
                            sender.sendMessage(ChatColor.RED + "Why would you message yourself? lmao");
                            return;
                        }
                        msgStart = 1;
                    } else {
                        sender.sendMessage(ChatColor.RED + "The target you specified is not online.");
                        return;
                    }
                    break;
                case "reply":
                case "r":
                    if (profile.getLastRecipient() != null) {
                        target = Bukkit.getPlayer(profile.getLastRecipient());
                        if (target == null) {
                            sender.sendMessage(ChatColor.RED + "The person you were trying to reply to is no longer online.");
                            return;
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You have not messaged anyone since you have logged on.");
                        return;
                    }

                    break;
                default:
                    return;
            }

            if (profile.getIgnored().contains(target.getUniqueId())) {
                sender.sendMessage(ChatColor.RED + "Why would you send a message to someone you ignored? It wasn't sent.");
                return;
            }

            Profile targetProfile = core.getProfileManager().get(target.getUniqueId());
            if (targetProfile.getIgnored().contains(player.getUniqueId()) || !targetProfile.getSettings().isPrivateMessages()) {
                sender.sendMessage(ChatColor.RED + "The person you attempted to message is not accepting PMs right now.");
                return;
            }

            if (args.length <= msgStart) {
                sender.sendMessage(ChatColor.RED + "You cannot send a blank message.");
                return;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = msgStart; i < args.length; i++) {
                sb.append(args[i] + " ");
            }

            profile.setLastRecipient(target.getUniqueId());
            targetProfile.setLastRecipient(player.getUniqueId());
            player.sendMessage(Colors.get("&7(To " + targetProfile.getHighestRank().getColor() + target.getName() + "&7) &r" + sb.toString()));
            target.sendMessage(Colors.get("&7(From " + profile.getHighestRank().getColor() + player.getName() + "&7) &r" + sb.toString()));
            target.playSound(target.getLocation(), Sound.LEVEL_UP, 1.0F, 2.0F);
        }
    }
}
