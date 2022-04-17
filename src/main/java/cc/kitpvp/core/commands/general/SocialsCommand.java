package cc.kitpvp.core.commands.general;

import cc.kitpvp.core.commands.BaseCommand;
import org.bukkit.command.CommandSender;

public class SocialsCommand extends BaseCommand {

    public SocialsCommand(String name) {
        super(name, "&a&lSocial Media &7(Page <page_number>/<pages>)");
        getCommandHelper()
                .addEntry("&eDiscord &7- &dddddd")
                .addEntry("&eTwitter &7- &fhttps://twitter.com/mcpanda");
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        sender.sendMessage(getCommandHelper().getMessage(1));
    }
}