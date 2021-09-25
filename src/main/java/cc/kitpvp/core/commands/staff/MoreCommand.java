package cc.kitpvp.core.commands.staff;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.commands.BaseCommand;
import cc.kitpvp.core.utils.RandomNoPermission;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MoreCommand extends BaseCommand {

    private final Core core;

    public MoreCommand(Core core, String name) {
        super(name);
        this.core = core;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission("core.more")) {
            sender.sendMessage(RandomNoPermission.getRandomPermission());
            return;
        }

        if(sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack item = player.getItemInHand();
            if(item != null) {
                item.setAmount(64);
                player.sendMessage(ChatColor.GREEN + "There you go.");
            } else {
                player.sendMessage(ChatColor.RED + "Want more nothing? Idiot.");
            }
        }
    }
}
