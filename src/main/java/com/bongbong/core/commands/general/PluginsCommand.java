package com.bongbong.core.commands.general;

import com.bongbong.core.commands.BaseCommand;
import com.bongbong.core.utils.Colors;
import org.bukkit.command.CommandSender;

public class PluginsCommand extends BaseCommand {

    public PluginsCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        sender.sendMessage(Colors.get(""));
        sender.sendMessage(Colors.get("&7&m---------------------------------------------"));
        sender.sendMessage(Colors.get("&a&lSERVERSIDE SOFTWARE & TECHNOLOGIES"));
        sender.sendMessage(Colors.get(""));
        sender.sendMessage(Colors.get("&eWe take pride in contributing to the community and leading by"));
        sender.sendMessage(Colors.get("&eexample. This list is intended to help server owners inspired"));
        sender.sendMessage(Colors.get("&eby our server. We maintain multiple open-sourced projects."));
        sender.sendMessage(Colors.get(""));
        sender.sendMessage(Colors.get("&e&nInfrastructure"));
        sender.sendMessage(Colors.get("&fDebian 10 &7(www.debian.org/ - DFSG)"));
        sender.sendMessage(Colors.get("&fJava 8 &7(www.oracle.com/ - OTN)"));
        sender.sendMessage(Colors.get("&fRedis &7(www.redis.io/ - BSD)"));
        sender.sendMessage(Colors.get("&fMariaDB &7(www.mariadb.org/ - GPLv2)"));
        sender.sendMessage(Colors.get(""));
        sender.sendMessage(Colors.get("&e&nMinecraft Server Software"));
        sender.sendMessage(Colors.get("&fImanitySpigot3 &7(www.mc.mk/resources/10770/ - Proprietary)"));
        sender.sendMessage(Colors.get(""));
        sender.sendMessage(Colors.get("&e&nMinecraft Proxy Software"));
        sender.sendMessage(Colors.get("&fXCord &7(www.mc.mk/resources/16843/ - Proprietary)"));
        sender.sendMessage(Colors.get(""));
        sender.sendMessage(Colors.get("&e&nServer Plugins &7(11)"));
        sender.sendMessage(Colors.get("&fBasics &7(www.github.com/TheoBong - Proprietary)"));
        sender.sendMessage(Colors.get("&fKitCore &7(www.github.com/TheoBong - Proprietary)"));
        sender.sendMessage(Colors.get("&fDomainReferrals &7(www.github.com/TheoBong - MIT)"));
        sender.sendMessage(Colors.get("&fStaffSuite &7(www.github.com/TheoBong - GPLv3)"));
        sender.sendMessage(Colors.get("&fStore &7(www.github.com/TheoBong - MIT)"));
        sender.sendMessage(Colors.get("&fCitizens &7(www.github.com/CitizensDev - OSLv3)"));
        sender.sendMessage(Colors.get("&fProtocolSupport &7(www.github.com/ProtocolSupport - AGPLv3)"));
        sender.sendMessage(Colors.get("&fJumpPads &7(www.github.com/Benz56 - Unlicensed)"));
        sender.sendMessage(Colors.get("&fViaVersion &7(www.github.com/ViaVersion - GPLv3)"));
        sender.sendMessage(Colors.get("&fProtocolLib &7(www.github.com/dmulloy2 - GPLv2)"));
        sender.sendMessage(Colors.get("&fHolographicDisplays &7(www.github.com/filoghost - GPLv3)"));
        sender.sendMessage(Colors.get("&7&m---------------------------------------------"));
        sender.sendMessage(Colors.get(""));
    }
}