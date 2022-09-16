package com.bongbong.core.commands.general;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.commands.BaseCommand;
import com.bongbong.core.networking.CoreRedisAction;
import com.bongbong.core.networking.redis.RedisMessage;
import com.bongbong.core.profiles.Profile;
import com.bongbong.core.utils.Colors;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.Date;

public class ReportCommand extends BaseCommand {

    private final CorePlugin plugin;

    public ReportCommand(CorePlugin plugin, String name) {
        super(name);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if(sender instanceof Player) {
            if(args.length > 1) {
                Player player = (Player) sender;
                Profile profile = plugin.getProfileManager().get(player.getUniqueId());
                Player target = Bukkit.getPlayer(args[0]);

                Date date = profile.getCooldowns().get(Profile.Cooldown.REPORT);
                if(!(date == null || date.before(new Date()))) {
                    sender.sendMessage(ChatColor.RED + "You have to wait a minute after your last report to send another report.");
                    return;
                }

                if(target != null) {

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    calendar.add(Calendar.MINUTE, 1);
                    profile.getCooldowns().put(Profile.Cooldown.REPORT, calendar.getTime());

                    StringBuilder sb = new StringBuilder();
                    for(int i = 1; i < args.length; i++) {
                        sb.append(args[i]);
                        if(i + 1 != args.length) {
                            sb.append(" ");
                        }
                    }

                    StringBuilder reportSb = new StringBuilder();
                    reportSb.append("&f &f\n&7[Staff] &f" + player.getName() + "&a reported &f" + target.getName() + " &aon server &f" + plugin.getConfig().getString("general.server_name") + "&a for &f" + sb.toString() + "\n&f &f");

                    JsonObject json = new JsonObject();
                    json.addProperty("action", CoreRedisAction.STAFF_BROADCAST.toString());
                    json.addProperty("message", reportSb.toString());
                    plugin.getRedisPublisher().getMessageQueue().add(new RedisMessage("core", json));

                    player.sendMessage(Colors.get("&aYou reported &f" + target.getName() + "&a for: &f" + sb.toString() +
                            "\n&7Your report has been sent to all online staff members."));
                } else {
                    sender.sendMessage(ChatColor.RED + "The target you specified is not on this server.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /report <player> <reason>");
            }
        }
    }
}
