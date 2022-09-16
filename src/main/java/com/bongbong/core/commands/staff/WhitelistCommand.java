package com.bongbong.core.commands.staff;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.commands.BaseCommand;
import com.bongbong.core.server.CoreServer;
import com.bongbong.core.server.WhitelistMode;
import com.bongbong.core.utils.Colors;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WhitelistCommand extends BaseCommand {

    private final CorePlugin plugin;

    public WhitelistCommand(CorePlugin plugin, String name) {
        super(name);
        this.plugin = plugin;
        this.setAliases("wl");
        this.setUsage(Colors.get("&cUsage: /whitelist <staff/donors/off>"));
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission("core.whitelist")) {
            sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(usageMessage);
            return;
        }

        final CoreServer settings = plugin.getCoreServer();

        switch (args[0].toLowerCase()) {
            case "none":
            case "off":
                settings.setWhitelistMode(WhitelistMode.NONE);
                settings.export(plugin.getConfig());
                break;
            case "ranks":
            case "vips":
            case "donors":
                settings.setWhitelistMode(WhitelistMode.DONOR);
                settings.export(plugin.getConfig());
                break;
            case "staff":
            case "on":
                settings.setWhitelistMode(WhitelistMode.STAFF);
                settings.export(plugin.getConfig());
                break;
            default:
                sender.sendMessage(Colors.get("&cThat's not a valid whitelist mode!"));
                return;
        }

        final WhitelistMode whitelistMode = settings.getWhitelistMode();
        final Server server = plugin.getServer();

        if (whitelistMode == WhitelistMode.NONE) {
            server.broadcastMessage(Colors.get("&cThe server is no longer whitelisted!"));
        } else {
            whitelistMode.activate(plugin);
            server.broadcastMessage(Colors.get("&cThe server is now whitelisted (Mode: " + whitelistMode + ")."));
        }
    }
}
