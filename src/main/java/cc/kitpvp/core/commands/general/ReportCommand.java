package cc.kitpvp.core.commands.general;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.commands.BaseCommand;
import cc.kitpvp.core.networking.CoreRedisAction;
import cc.kitpvp.core.networking.redis.RedisMessage;
import cc.kitpvp.core.profiles.Profile;
import cc.kitpvp.core.utils.Colors;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.Date;

public class ReportCommand extends BaseCommand {

    private final Core core;

    public ReportCommand(Core core, String name) {
        super(name);
        this.core = core;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if(sender instanceof Player) {
            if(args.length > 1) {
                Player player = (Player) sender;
                Profile profile = core.getProfileManager().get(player.getUniqueId());
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
                    reportSb.append("&f &f\n&7[Staff] &f" + player.getName() + "&a reported &f" + target.getName() + " &aon server &f" + core.getConfig().getString("general.server_name") + "&a for &f" + sb.toString() + "\n&f &f");

                    JsonObject json = new JsonObject();
                    json.addProperty("action", CoreRedisAction.STAFF_BROADCAST.toString());
                    json.addProperty("message", reportSb.toString());
                    core.getRedisPublisher().getMessageQueue().add(new RedisMessage("core", json));

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
