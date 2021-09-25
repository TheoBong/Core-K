package cc.kitpvp.core.commands.general;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.commands.BaseCommand;
import org.bukkit.command.CommandSender;

public class SocialsCommand extends BaseCommand {

    private final Core core;

    public SocialsCommand(Core core, String name) {
        super(name, "&a&lSocial Media &7(Page <page_number>/<pages>)");
        this.core = core;

        getCommandHelper()
                .addEntry("&eDiscord &7- &fhttps://discord.gg/aGtDd6ss")
                .addEntry("&eTwitter &7- &fhttps://twitter.com/kitpvpcc");
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        sender.sendMessage(getCommandHelper().getMessage(1));
    }
}