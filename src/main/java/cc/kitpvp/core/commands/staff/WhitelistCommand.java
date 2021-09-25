package cc.kitpvp.core.commands.staff;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.commands.BaseCommand;
import cc.kitpvp.core.server.CoreServer;
import cc.kitpvp.core.server.WhitelistMode;
import cc.kitpvp.core.utils.Colors;
import cc.kitpvp.core.utils.RandomNoPermission;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WhitelistCommand extends BaseCommand {

    private final Core core;

    public WhitelistCommand(Core core, String name) {
        super(name);
        this.core = core;
        this.setAliases("wl");
        this.setUsage(Colors.get("&cUsage: /whitelist <staff/donors/off>"));
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission("core.whitelist")) {
            sender.sendMessage(RandomNoPermission.getRandomPermission());
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(usageMessage);
            return;
        }

        final CoreServer settings = core.getCoreServer();

        switch (args[0].toLowerCase()) {
            case "none":
            case "off":
                settings.setWhitelistMode(WhitelistMode.NONE);
                settings.export(core.getConfig());
                break;
            case "ranks":
            case "vips":
            case "donors":
                settings.setWhitelistMode(WhitelistMode.DONOR);
                settings.export(core.getConfig());
                break;
            case "staff":
            case "on":
                settings.setWhitelistMode(WhitelistMode.STAFF);
                settings.export(core.getConfig());
                break;
            default:
                sender.sendMessage(Colors.get("&cThat's not a valid whitelist mode!"));
                return;
        }

        final WhitelistMode whitelistMode = settings.getWhitelistMode();
        final Server server = core.getServer();

        if (whitelistMode == WhitelistMode.NONE) {
            server.broadcastMessage(Colors.get("&cThe server is no longer whitelisted!"));
        } else {
            whitelistMode.activate();
            server.broadcastMessage(Colors.get("&cThe server is now whitelisted (Mode: " + whitelistMode + ")."));
        }
    }
}
