package cc.kitpvp.core.commands.ranks;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.commands.BaseCommand;
import cc.kitpvp.core.ranks.Rank;
import cc.kitpvp.core.utils.Colors;
import cc.kitpvp.core.utils.RandomNoPermission;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class RankCommand extends BaseCommand {

    private final Core core;

    public RankCommand(Core core, String name) {
        super(name,"&a&lRank Manager Help &7(Page <page_number>/<pages>)");
        this.core = core;
        this.addAlias("rm");
        getCommandHelper()
                .addEntry("&e/rank list &7- &fLists all ranks.")
                .addEntry("&e/rank info <name> &7- &fGets a rank's information.")
                .addEntry("&e/rank create <name> <weight> &7- &fCreates a new rank.")
                .addEntry("&e/rank delete <name> &7- &fDeletes an existing rank.")
                .addEntry("&e/rank rename <name> <new name> &7- &fRenames a rank.")
                .addEntry("&e/rank setprefix <name> <prefix> &7- &fSets a rank's prefix.")
                .addEntry("&e/rank setcolor <name> <color> &7- &fSets a rank's color.")
                .addEntry("&e/rank setdisplayname <name> <display name> &7- &fSets a rank's display name.")
                .addEntry("&e/rank setdefault <name> <boolean> &7- &fSets a rank to be automatically given upon creation of a new profile.")
                .addEntry("&e/rank setnamemc <name> <boolean> &7- &6&lNew! &fSets a rank to be automatically given if they liked the server on NameMC.")
                .addEntry("&e/rank addparent <name> <parent> &7- &fAdds a parent to a rank.")
                .addEntry("&e/rank removeparent <name> <parent> &7- &fRemoves a parent from a rank.")
                .addEntry("&e/rank permissions <name> [server] &7- &fGets a rank's permissions, optional server argument.")
                .addEntry("&e/rank addpermission <name> <permission> [boolean] [server] &7- &fAdds a permission to a rank, optional server argument.")
                .addEntry("&e/rank removepermission <name> <permission> [server] &7- &fRemoves a permission from a rank, optional server argument.");
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission("core.ranks")) {
            sender.sendMessage(RandomNoPermission.getRandomPermission());
            return;
        }

        if(args.length > 0) {
            switch(args[0].toLowerCase()) {
                case "list":
                    Map<UUID, Rank> ranks = core.getRankManager().getRanks();

                    if(ranks.isEmpty()) {
                        sender.sendMessage(ChatColor.RED + "There are no ranks, please create one with /rm create.");
                        break;
                    }

                    StringBuilder sb = new StringBuilder();
                    TreeMap<Integer, Rank> sortedRanks = new TreeMap<>();
                    for(Rank rank : ranks.values()) {
                        sortedRanks.put(rank.getWeight(), rank);
                    }

                    sb.append("&a&lRanks:");
                    for(Rank rank : sortedRanks.descendingMap().values()) {
                        sb.append("\n&f" + rank.getWeight() + " &7- &e" + rank.getDisplayName() + " &7(" + rank.getName() + ")");
                        if(rank.isDefaultRank()) {
                            sb.append(" &6(default)");
                        }
                    }

                    sender.sendMessage(Colors.get(sb.toString()));
                    break;
                case "info":
                    if(args.length > 1) {
                        Rank rank = core.getRankManager().getRank(args[1]);
                        if(rank == null) {
                            sender.sendMessage(ChatColor.RED + "The rank you specified does not exist.");
                            break;
                        }

                        sb = new StringBuilder();
                        sb.append("&eName: &f" + rank.getName());
                        sb.append("\n&eDisplay Name: &f" + rank.getDisplayName());
                        sender.sendMessage(Colors.get(sb.toString()));
                    } else {
                        sender.sendMessage(getCommandHelper().getMessage(1));
                    }
                    break;
                case "create":
                    if(args.length > 2) {
                        int weight;
                        try {
                            weight = Integer.parseInt(args[2]);
                            if(weight < 0) {
                                sender.sendMessage(ChatColor.RED + "A rank's weight cannot be negative.");
                                break;
                            }
                        } catch(NumberFormatException e) {
                            sender.sendMessage(ChatColor.RED + "You must specify a number for the weight.");
                            break;
                        }

                        Rank rank = core.getRankManager().createRank(Colors.strip(args[1]), weight);
                        if(rank != null) {
                            sender.sendMessage(ChatColor.GREEN + "Rank " + ChatColor.WHITE + rank.getName() + ChatColor.GREEN + " with a weight of " + ChatColor.WHITE + weight + ChatColor.GREEN + " has been created.");
                        } else {
                            sender.sendMessage(ChatColor.RED + "There is already a rank with the name or weight you specified.");
                        }
                    } else {
                        sender.sendMessage(getCommandHelper().getMessage(1));
                    }
                    break;
                case "delete":
                case "remove":
                    if(args.length > 1) {
                        Rank rank = core.getRankManager().getRank(args[1]);
                        if(rank == null) {
                            sender.sendMessage(ChatColor.RED + "The rank you specified does not exist.");
                            break;
                        }

                        core.getRankManager().remove(true, rank);
                        sender.sendMessage(ChatColor.GREEN + "Rank " + ChatColor.WHITE + rank.getName() + ChatColor.GREEN + " has been removed.");
                    } else {
                        sender.sendMessage(getCommandHelper().getMessage(1));
                    }
                    break;
                case "rename":
                    if(args.length > 2) {
                        Rank rank = core.getRankManager().getRank(args[1]);
                        Rank newName = core.getRankManager().getRank(args[2]);
                        if(rank == null || newName != null) {
                            sender.sendMessage(ChatColor.RED + "One of the rank names you specified is invalid or already exists (new name).");
                            break;
                        }

                        final String old = rank.getName();
                        rank.setName(Colors.strip(args[2].toLowerCase()));
                        core.getRankManager().push(true, rank);
                        sender.sendMessage(ChatColor.GREEN + "Rank " + ChatColor.WHITE + old + ChatColor.GREEN + " has been renamed to " + ChatColor.WHITE + rank.getName() + ChatColor.GREEN + ".");
                    } else {
                        sender.sendMessage(getCommandHelper().getMessage(1));
                    }
                    break;
                case "setprefix":
                    if(args.length > 2) {
                        Rank rank = core.getRankManager().getRank(args[1]);
                        if(rank == null) {
                            sender.sendMessage(ChatColor.RED + "The rank you specified does not exist.");
                            break;
                        }

                        sb = new StringBuilder();
                        for(int i = 2; i < args.length; i++) {
                            sb.append(args[i]);
                            if(i + 1 != args.length) {
                                sb.append(" ");
                            }
                        }

                        rank.setPrefix(sb.toString());
                        core.getRankManager().push(true, rank);
                        sender.sendMessage(ChatColor.GREEN + "Rank " + ChatColor.WHITE + rank.getName() + ChatColor.GREEN + " now has the prefix: " + ChatColor.RESET + Colors.get(rank.getPrefix()));
                    } else {
                        sender.sendMessage(getCommandHelper().getMessage(1));
                    }
                    break;
                case "setcolor":
                    if(args.length > 2) {
                        Rank rank = core.getRankManager().getRank(args[1]);
                        if(rank == null) {
                            sender.sendMessage(ChatColor.RED + "The rank you specified does not exist.");
                            break;
                        }

                        rank.setColor(args[2]);
                        core.getRankManager().push(true, rank);
                        sender.sendMessage(ChatColor.GREEN + "Rank " + ChatColor.WHITE + rank.getName() + ChatColor.GREEN + " has a new color: " + Colors.get(rank.getColor() + "color"));
                    } else {
                        sender.sendMessage(getCommandHelper().getMessage(1));
                    }
                    break;
                case "setdisplayname":
                    if(args.length > 2) {
                        Rank rank = core.getRankManager().getRank(args[1]);
                        if(rank == null) {
                            sender.sendMessage(ChatColor.RED + "The rank you specified does not exist.");
                            break;
                        }

                        sb = new StringBuilder();
                        for(int i = 2; i < args.length; i++) {
                            sb.append(args[i]);
                            if(i + 1 != args.length) {
                                sb.append(" ");
                            }
                        }

                        rank.setDisplayName(sb.toString());
                        core.getRankManager().push(true, rank);
                        sender.sendMessage(ChatColor.GREEN + "Rank " + ChatColor.WHITE + rank.getName() + ChatColor.GREEN + " has a new display name: " + ChatColor.RESET + Colors.get(rank.getDisplayName()));
                    } else {
                        sender.sendMessage(getCommandHelper().getMessage(1));
                    }
                    break;
                case "setdefault":
                    if(args.length > 2) {
                        Rank rank = core.getRankManager().getRank(args[1]);
                        if(rank == null) {
                            sender.sendMessage(ChatColor.RED + "The rank you specified does not exist.");
                            break;
                        }

                        boolean b = Boolean.parseBoolean(args[2]);
                        if(b && rank.isDefaultRank()) {
                            sender.sendMessage(ChatColor.RED + "The rank you specified is already the default rank.");
                            break;
                        } else if(!b && rank.isDefaultRank()) {
                            sender.sendMessage(ChatColor.RED + "You must set another rank as the default rank, as there must always be a default rank.");
                            break;
                        }

                        Rank defaultRank = core.getRankManager().getDefaultRank();
                        if(defaultRank != null) {
                            defaultRank.setDefaultRank(false);
                            core.getRankManager().push(true, defaultRank);
                            sender.sendMessage(ChatColor.GREEN + "Rank " + ChatColor.WHITE + rank.getName() + ChatColor.GREEN + " is now the default rank instead of " + ChatColor.WHITE + defaultRank.getName() + ChatColor.GREEN + ".");
                        } else {
                            sender.sendMessage(ChatColor.GREEN + "Rank " + ChatColor.WHITE + rank.getName() + ChatColor.GREEN + " is now the default rank.");
                        }

                        rank.setDefaultRank(true);
                        core.getRankManager().push(true, rank);


                    } else {
                        sender.sendMessage(getCommandHelper().getMessage(1));
                    }
                    break;
                case "addparent":
                    if(args.length > 2) {
                        Rank rank = core.getRankManager().getRank(args[1]);
                        Rank parent = core.getRankManager().getRank(args[2]);
                        if(rank == null || parent == null) {
                            sender.sendMessage(ChatColor.RED + "One of the ranks you specified does not exist.");
                            break;
                        }

                        if(rank.getParents().contains(parent.getUuid())) {
                            sender.sendMessage(ChatColor.RED + "Rank " + rank.getName() + " already has this parent.");
                            break;
                        }

                        rank.getParents().add(parent.getUuid());
                        core.getRankManager().push(true, rank);
                        sender.sendMessage(ChatColor.GREEN + "Rank " + ChatColor.WHITE + rank.getName() + ChatColor.GREEN + " has a new parent rank: " + ChatColor.WHITE + parent.getName());
                    } else {
                        sender.sendMessage(getCommandHelper().getMessage(1));
                    }
                    break;
                case "removeparent":
                case "deleteparent":
                    if(args.length > 2) {
                        Rank rank = core.getRankManager().getRank(args[1]);
                        Rank parent = core.getRankManager().getRank(args[2]);
                        if(rank == null || parent == null) {
                            sender.sendMessage(ChatColor.RED + "One of the ranks you specified does not exist.");
                            break;
                        }

                        if(!rank.getParents().contains(parent.getUuid())) {
                            sender.sendMessage(ChatColor.RED + "Rank " + rank.getName() + " doesn't have this parent.");
                            break;
                        }

                        rank.getParents().remove(parent.getUuid());
                        core.getRankManager().push(true, rank);
                        sender.sendMessage(ChatColor.GREEN + "Rank " + ChatColor.WHITE + rank.getName() + ChatColor.GREEN + " no longer has this parent rank: " + ChatColor.WHITE + parent.getName());
                    } else {
                        sender.sendMessage(getCommandHelper().getMessage(1));
                    }
                    break;
                case "permissions":
                case "perms":
                    if(args.length > 1) {
                        Rank rank = core.getRankManager().getRank(args[1]);
                        if(rank == null) {
                            sender.sendMessage(ChatColor.RED + "The rank you specified does not exist.");
                            break;
                        }

                        String server = ".global";
                        if(args.length > 2) {
                            if (args[2].equalsIgnoreCase(server)) {
                                sender.sendMessage(ChatColor.RED + "Invalid server name.");
                                break;
                            }
                            server = args[2].toLowerCase();
                        }

                        Map<String, Boolean> perms = rank.getSpecificPermissions(server);
                        if(perms != null && !perms.isEmpty()) {
                            sb = new StringBuilder();
                            sb.append("&aPermissions &7(" + server + ")&a:");
                            for(Map.Entry<String, Boolean> entry : perms.entrySet()) {
                                sb.append((entry.getValue() ? " &e" : " &c") + entry.getKey());
                            }

                            sender.sendMessage(Colors.get(sb.toString()));
                        } else {
                            sender.sendMessage(ChatColor.RED + "No permissions set for scope '" + server + "'.");
                        }

                    } else {
                        sender.sendMessage(getCommandHelper().getMessage(1));
                    }
                    break;
                case "addpermission":
                case "addperm":
                    if(args.length > 2) {
                        Rank rank = core.getRankManager().getRank(args[1]);
                        if(rank == null) {
                            sender.sendMessage(ChatColor.RED + "The rank you specified does not exist.");
                            break;
                        }

                        String server = ".global";
                        if(args.length > 3) {
                            if (args[3].equalsIgnoreCase(server)) {
                                sender.sendMessage(ChatColor.RED + "Invalid server name.");
                                break;
                            }
                            server = args[3].toLowerCase();
                        }

                        boolean b = true;
                        if(args.length > 4) {
                            b = Boolean.parseBoolean(args[4]);
                        }

                        Map<String, Boolean> perms = rank.getSpecificPermissions(server);
                        if(perms == null) {
                            perms = new HashMap<>();
                            rank.getPermissions().put(server, perms);
                        }

                        if(perms.get(args[2].toLowerCase()) != null && perms.get(args[2].toLowerCase()) == b) {
                            sender.sendMessage(ChatColor.RED + "That permission has already been set.");
                            break;
                        }

                        perms.put(args[2].toLowerCase(), b);
                        core.getRankManager().push(true, rank);
                        sender.sendMessage(ChatColor.GREEN + "Rank " + ChatColor.WHITE + rank.getName() + ChatColor.GREEN + " has a new permission set for scope " + ChatColor.WHITE + server + ChatColor.GREEN +
                                ": " + ChatColor.WHITE + args[2].toLowerCase() + ChatColor.GRAY + " (" + (b ? "true" : "false") + ")");
                    } else {
                        sender.sendMessage(getCommandHelper().getMessage(1));
                    }
                    break;
                case "removepermission":
                case "removeperm":
                    if(args.length > 2) {
                        Rank rank = core.getRankManager().getRank(args[1]);
                        if(rank == null) {
                            sender.sendMessage(ChatColor.RED + "The rank you specified does not exist.");
                            break;
                        }

                        String server = ".global";
                        if(args.length > 3) {
                            if (args[3].equalsIgnoreCase(server)) {
                                sender.sendMessage(ChatColor.RED + "Invalid server name.");
                                break;
                            }
                            server = args[3].toLowerCase();
                        }

                        Map<String, Boolean> perms = rank.getSpecificPermissions(server);
                        if(perms == null) {
                            perms = new HashMap<>();
                            rank.getPermissions().put(server, perms);
                        }

                        if(perms.get(args[2].toLowerCase()) == null) {
                            sender.sendMessage(ChatColor.RED + "That permission is not set.");
                            break;
                        }

                        perms.remove(args[2].toLowerCase());
                        core.getRankManager().push(true, rank);
                        sender.sendMessage(ChatColor.GREEN + "Rank " + ChatColor.WHITE + rank.getName() + ChatColor.GREEN + " had a permission removed from scope " + ChatColor.WHITE + server + ChatColor.GREEN +
                                ": " + ChatColor.WHITE + args[2].toLowerCase());
                    } else {
                        sender.sendMessage(getCommandHelper().getMessage(1));
                    }
                    break;
                default:
                    sender.sendMessage(getCommandHelper().getMessage(1));
            }
        } else {
            sender.sendMessage(getCommandHelper().getMessage(1));
        }
    }
}
