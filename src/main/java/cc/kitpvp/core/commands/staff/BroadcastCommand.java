package cc.kitpvp.core.commands.staff;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.commands.BaseCommand;
import cc.kitpvp.core.networking.CoreRedisAction;
import cc.kitpvp.core.networking.redis.RedisMessage;
import cc.kitpvp.core.utils.RandomNoPermission;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BroadcastCommand extends BaseCommand {

    private final Core core;

    public BroadcastCommand(Core core, String name) {
        super(name);
        this.core = core;
        this.setAliases("staffbroadcast");
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission("core.broadcast")) {
            sender.sendMessage(RandomNoPermission.getRandomPermission());
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
            core.getRedisPublisher().getMessageQueue().add(new RedisMessage("core", json));
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /" + alias.toLowerCase() + " <message>");
        }
    }
}
