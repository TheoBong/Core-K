package com.bongbong.core.commands.general;

import com.bongbong.core.commands.BaseCommand;
import org.bukkit.command.CommandSender;

public class SocialsCommand extends BaseCommand {

    public SocialsCommand(String name) {
        super(name, "&a&lSocial Media &7(Page <page_number>/<pages>)");
        getCommandHelper()
                .addEntry("&eDiscord &7- &fhttps://discord.gg/rM6gWKWE9u")
                .addEntry("&eTwitter &7- &fhttps://twitter.com/kitpvpcc");
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {

        sender.sendMessage(getCommandHelper().getMessage(1));
    }
}