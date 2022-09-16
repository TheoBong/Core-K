package com.bongbong.core.commands;

import com.bongbong.core.utils.CommandHelper;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class BaseCommand extends Command {
    private @Getter CommandHelper commandHelper;

    public BaseCommand(final String name) {
        super(name);
    }

    public BaseCommand(String name, String commandHelperTitle) {
        super(name);
        if(commandHelperTitle != null) {
            this.commandHelper = new CommandHelper(commandHelperTitle);
        }
    }

    @Override
    public final boolean execute(final CommandSender sender, final String alias, final String[] args) {

        if(commandHelper != null) {
            if(args.length > 0 && args[0].equalsIgnoreCase("help")) {
                if(args.length > 1) {
                    try {
                        final int page = Integer.parseInt(args[1]);
                        sender.sendMessage(commandHelper.getMessage(page));
                        return true;
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid help page number.");
                        return true;
                    }
                } else {
                    sender.sendMessage(commandHelper.getMessage(1));
                    return true;
                }
            }
        }

        this.execute(sender, args, alias);
        return true;
    }

    public void setAliases(final String... aliases) {
        if (aliases.length > 0) {
            this.setAliases(aliases.length == 1 ? Collections.singletonList(aliases[0]) : Arrays.asList(aliases));
        }
    }

    public BaseCommand addAlias(String s) {
        List<String> aliases = getAliases();
        if(aliases == null) {
            aliases = new ArrayList<>();
            setAliases(aliases);
        }

        aliases.add(s);
        return this;
    }

    public abstract void execute(CommandSender sender, String[] args, String alias);
}
