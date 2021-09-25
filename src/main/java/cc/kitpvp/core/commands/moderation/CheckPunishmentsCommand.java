package cc.kitpvp.core.commands.moderation;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.commands.BaseCommand;
import cc.kitpvp.core.profiles.Profile;
import cc.kitpvp.core.punishments.Punishment;
import cc.kitpvp.core.utils.Colors;
import cc.kitpvp.core.utils.RandomNoPermission;
import cc.kitpvp.core.utils.ThreadUtil;
import cc.kitpvp.core.web.WebPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import xyz.leuo.gooey.button.Button;
import xyz.leuo.gooey.gui.PaginatedGUI;

import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

public class CheckPunishmentsCommand extends BaseCommand {

    private final Core core;

    public CheckPunishmentsCommand(Core core, String name) {
        super(name);
        this.core = core;
        this.setAliases("c", "history");
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission("core.staff")) {
            sender.sendMessage(RandomNoPermission.getRandomPermission());
            return;
        }

        if(sender instanceof Player) {
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /c <player>");
                return;
            }

            ThreadUtil.runTask(true, core, () -> {
                Player staff = (Player) sender;
                UUID uuid;
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    WebPlayer webPlayer = new WebPlayer(args[0]);
                    uuid = webPlayer.getUuid();
                } else {
                    uuid = target.getUniqueId();
                }

                Profile profile;
                profile = core.getProfileManager().get(uuid);
                if (profile == null) {
                    profile = core.getProfileManager().find(uuid, false);
                }

                PaginatedGUI gui = new PaginatedGUI(profile.getName() + "'s Punishments", 18);

                List<Punishment> punishments = profile.getPunishmentsTest();
                TreeMap<Date, Punishment> map = new TreeMap<>();

                for(Punishment p : punishments) {
                    if(p.isActive()) {
                        gui.addButton(createButton(p));
                        continue;
                    }

                    map.put(p.getIssued(), p);
                }

                for(Punishment p : map.descendingMap().values()) {
                    gui.addButton(createButton(p));
                }

                core.getServer().getScheduler().runTask(core, () -> {
                    gui.open(staff);
                });
            });
        }
    }

    public Button createButton(Punishment punishment) {
        Button button;
        switch (punishment.getType()) {
            case BLACKLIST: button = new Button(Material.IRON_FENCE,
                    punishment.isActive() ? Colors.get("&6&l" + punishment.getUuid().toString()) : Colors.get("&6" + punishment.getUuid().toString())); break;
            case BAN: button = new Button(Material.LADDER,
                    punishment.isActive() ? Colors.get("&6&l" + punishment.getUuid().toString()) : Colors.get("&6" + punishment.getUuid().toString())); break;
            case MUTE: button = new Button(Material.ACTIVATOR_RAIL,
                    punishment.isActive() ? Colors.get("&6&l" + punishment.getUuid().toString()) : Colors.get("&6" + punishment.getUuid().toString())); break;
            case KICK: button = new Button(Material.VINE,
                    punishment.isActive() ? Colors.get("&6&l" + punishment.getUuid().toString()) : Colors.get("&6" + punishment.getUuid().toString())); break;
            default: button = new Button(Material.WOOD_SWORD,
                    punishment.isActive() ? Colors.get("&6&l" + punishment.getUuid().toString()) : Colors.get("&6" + punishment.getUuid().toString())); break;
        }

        if(punishment.isActive()) {
            button.getMeta().addEnchant(Enchantment.DURABILITY, 1, true);
        }

        String issuerName;
        if (punishment.getIssuer() == null) {
            issuerName = "Console";
        } else {
            Player issuer = Bukkit.getPlayer(punishment.getIssuer());
            if (issuer != null) {
                issuerName = issuer.getName();
            } else {
                WebPlayer wp = new WebPlayer(punishment.getIssuer());
                issuerName = wp.getName();
            }
        }

        String victimName;
        Player victim = Bukkit.getPlayer(punishment.getVictim());
        if (victim != null) {
            victimName = victim.getName();
        } else {
            WebPlayer wp = new WebPlayer(punishment.getVictim());
            victimName = wp.getName();
        }

        if (punishment.getPardoned() != null) {
            String pardonerName;

            if (punishment.getPardoner() == null) {
                pardonerName = "Console";
            } else {
                Player pardoner = Bukkit.getPlayer(punishment.getPardoner());
                if (pardoner != null) {
                    pardonerName = pardoner.getName();
                } else {
                    WebPlayer wp = new WebPlayer(punishment.getPardoner());
                    pardonerName = wp.getName();
                }
            }

            button.setLore(
                    "&7&m-----------------------------",
                    "&eTarget: &f" + victimName,
                    "&eType: &f" + punishment.getType(),
                    "&eActive: &f" + punishment.isActive(),
                    "&7&m-----------------------------",
                    "&eIssued on: &f" + punishment.getIssued().toString(),
                    "&eIssued by: &f" + issuerName,
                    "&eReason: &f" + punishment.getIssueReason(),
                    "&eExpiry: &f" + punishment.expiry(),
                    "&7&m-----------------------------",
                    "&ePardoned on: &f" + punishment.getPardoned(),
                    "&ePardoned by: &f" + pardonerName,
                    "&eReason: &f" + punishment.getPardonReason(),
                    "&7&m-----------------------------"
            );
        } else {
            button.setLore(
                    "&7&m-----------------------------",
                    "&eTarget: &f" + victimName,
                    "&eType: &f" + punishment.getType(),
                    "&eActive: &f" + punishment.isActive(),
                    "&7&m-----------------------------",
                    "&eIssued on: &f" + punishment.getIssued().toString(),
                    "&eIssued by: &f" + issuerName,
                    "&eReason: &f" + punishment.getIssueReason(),
                    "&eExpiry: &f" + punishment.expiry(),
                    "&7&m-----------------------------"
            );
        }
        button.setCloseOnClick(false);

        return button;
    }
}
