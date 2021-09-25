package cc.kitpvp.core.commands.moderation;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.commands.BaseCommand;
import cc.kitpvp.core.profiles.Profile;
import cc.kitpvp.core.punishments.Punishment;
import cc.kitpvp.core.utils.RandomNoPermission;
import cc.kitpvp.core.web.WebPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TempPunishCommand extends BaseCommand {

    private final Core core;

    public TempPunishCommand(Core core, String name) {
        super(name);
        this.core = core;

        this
        .addAlias("tempban")
        .addAlias("tban")
        .addAlias("tempmute")
        .addAlias("tmute");
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission("core.staff")) {
            sender.sendMessage(RandomNoPermission.getRandomPermission());
            return;
        }

        String label = alias.toLowerCase();
        if(args.length > 2) {
            Player target = Bukkit.getPlayer(args[0]);
            Profile profile = null;
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

            Punishment.Type punishmentType;
            switch (label) {
                case "tempban":
                case "tban":
                    punishmentType = Punishment.Type.BAN;
                    break;
                case "tempmute":
                case "tmute":
                    punishmentType = Punishment.Type.MUTE;
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Available commands: /tempban, /tempmute.");
                    return;
            }

            UUID issuer = null;
            if(sender instanceof Player) {
                Player player = (Player) sender;
                Profile pr = core.getProfileManager().get(player.getUniqueId());
                issuer = player.getUniqueId();
                if(pr.getHighestRank() != null && profile.getHighestRank() != null && (pr.getHighestRank().getWeight() < profile.getHighestRank().getWeight())) {
                    sender.sendMessage(ChatColor.RED + "You cannot punish someone who has a higher rank than you.");
                    return;
                }
            }

            Pattern p = Pattern.compile("[a-z]+|\\d+");
            Matcher m = p.matcher(args[1].toLowerCase());

            int time = -1;
            String type = null;
            boolean b = false;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            while (m.find()) {
                String a = m.group();
                try {
                    time = Integer.parseInt(a);
                    if(time < 1) {
                        time = -1;
                    }
                } catch(NumberFormatException e) {
                    type = a;
                }

                if(time > 0 && type != null) {
                    switch(type) {
                        case "seconds": case "second": case "sec": case "s":
                            calendar.add(Calendar.SECOND, time);
                            break;
                        case "minutes": case "minute": case "m":
                            calendar.add(Calendar.MINUTE, time);
                            break;
                        case "hours": case "hrs": case "hr": case "h":
                            calendar.add(Calendar.HOUR, time);
                            break;
                        case "days": case "day": case "d":
                            calendar.add(Calendar.HOUR, time * 24);
                            break;
                        case "weeks": case "week": case "w":
                            calendar.add(Calendar.HOUR, time * 24 * 7);
                            break;
                        case "months": case "month": case "mth":
                            calendar.add(Calendar.MONTH, time);
                            break;
                    }

                    b = true;
                    time = -1;
                    type = null;
                }
            }

            Punishment punishment = profile.getActivePunishment(punishmentType);
            if(punishment == null) {
                StringBuilder sb = new StringBuilder();
                boolean silent = false;
                for(int i = 2; i < args.length; i++) {
                    String s = args[i];
                    if(s.equalsIgnoreCase("-s")) {
                        silent = true;
                    } else {
                        sb.append(args[i]);
                        if (i + 1 != args.length) {
                            sb.append(" ");
                        }
                    }
                }

                if(b) {
                    profile.punish(punishmentType, issuer, sb.toString(), calendar.getTime(), silent);
                } else {
                    sender.sendMessage(ChatColor.RED + "You did not specify a valid timeframe.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "The target you specified already has an active punishment of that type.");
            }

        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <target> <timeframe> <reason>");
        }
    }
}
