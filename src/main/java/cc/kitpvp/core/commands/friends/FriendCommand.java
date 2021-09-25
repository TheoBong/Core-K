package cc.kitpvp.core.commands.friends;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.commands.BaseCommand;
import cc.kitpvp.core.profiles.Profile;
import cc.kitpvp.core.utils.ClickableMessage;
import cc.kitpvp.core.utils.Colors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;

public class FriendCommand extends BaseCommand {

    private final Core core;

    public FriendCommand(Core core, String name) {
        super(name, "&a&lFriend Manager Help &7(Page <page_number>/<pages>)");
        this.core = core;
        this.setAliases(
                "friends",
                "f");

        getCommandHelper()
                .addEntry("&e/friend add <player> &7- &fAdd a friend.")
                .addEntry("&e/friend remove <player> &7- &fRemove a friend.")
                .addEntry("&e/friend unrequest <player> &7- &fCancel an out-going friend request.")
                .addEntry("&e/friend list &7- &fList your friends.");
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        Player player = (Player) sender;
        Profile profile = core.getProfileManager().get(player.getUniqueId());

        if(args.length > 0) {
            switch(args[0].toLowerCase()) {
                case "list":
                    if (profile.getFriends() == null) {
                        sender.sendMessage(ChatColor.RED + "You have no friends.");
                        break;
                    }
                    player.sendMessage(Colors.get("&a&lFriends List &7(" + profile.getFriends().size() + ")"));
                    if(!profile.getFriends().isEmpty()) {
                        for (String friendName : profile.getFriends().values()) {
                            boolean online = false;
                            Player friendPlayer = null;

                            try { friendPlayer = Bukkit.getPlayer(friendName);} catch (Exception e) {}
                            if (friendPlayer != null) if (friendPlayer.isOnline()) online = true;

                            String onlineFormatted = online ? ChatColor.GREEN + " (Online)" : ChatColor.RED + " (Offline)";
                            player.sendMessage(ChatColor.GRAY + " - " + friendName + onlineFormatted);
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You have no friends.");
                    }
                    break;

                case "add":
                    if (args.length != 2) {
                        sender.sendMessage(getCommandHelper().getMessage(1));
                        break;
                    }

                    Player to = null;
                    try {
                        to = Bukkit.getPlayer(args[1]);
                    } catch (NullPointerException e) {
                        player.sendMessage(ChatColor.RED + "Invalid Player (maybe offline?)");
                    }

                    if (to != null) {
                        if (player == to) {
                            player.sendMessage("You cannot send yourself a friend request.");
                            break;
                        }

                        if (profile.getFriends().containsKey(to.getUniqueId().toString())) {
                            player.sendMessage("You are already friends with this player.");
                            break;
                        }

                        Profile toProfile = core.getProfileManager().get(to.getUniqueId());
                        if (toProfile.getPendingFriends() == null) {
                            profile.getPendingFriends().put(to.getUniqueId().toString(), to.getName());

                            player.sendMessage(ChatColor.GREEN + "[Friends]" + ChatColor.YELLOW + " Successfully sent friend request to " + to.getName());

                            to.sendMessage(ChatColor.GREEN + "[Friends] " + ChatColor.YELLOW + player.getName() + " wants to be your friend!");
                            ClickableMessage accept_button = new ClickableMessage(Colors.get("&7Click to accept " + player.getName() + "'s friend request!"))
                                    .hover(Colors.get("Click me!"))
                                    .command("/friend add " + player.getName());
                            accept_button.sendToPlayer(player);
                            break;
                        }

                        if (toProfile.getPendingFriends().containsKey((String) player.getUniqueId().toString())) { //checking if they added first
                            toProfile.getPendingFriends().remove((String) player.getUniqueId().toString());

                            toProfile.getFriends().put(player.getUniqueId().toString(), player.getName());
                            profile.getFriends().put(to.getUniqueId().toString(), to.getName());

                            player.sendMessage(ChatColor.GREEN + "[Friends]" + ChatColor.YELLOW + " You are now friends with " + to.getName());
                            if (to.isOnline()) to.sendMessage(ChatColor.GREEN + "[Friends] " + ChatColor.YELLOW + player.getName() + " accepted your friend request!");
                        } else { //this is if they didn't add first
                            profile.getPendingFriends().put(to.getUniqueId().toString(), to.getName());

                            player.sendMessage(ChatColor.GREEN + "[Friends]" + ChatColor.YELLOW + " Successfully sent friend request to " + to.getName());

                            to.sendMessage(ChatColor.GREEN + "[Friends] " + ChatColor.YELLOW + player.getName() + " wants to be your friend!");
                            ClickableMessage accept_button = new ClickableMessage(Colors.get("&7Click to accept " + player.getName() + "'s friend request!"))
                                    .hover(Colors.get("Click me!"))
                                    .command("/friend add " + player.getName());
                            accept_button.sendToPlayer(to);
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Invalid Player (maybe offline?)");
                    }
                    break;

                case "remove":
                    if (args.length != 2) {
                        sender.sendMessage(getCommandHelper().getMessage(1));
                        return;
                    }

                    Profile toProfile = null;

                    for (String key : profile.getFriends().keySet()) {
                        String name = profile.getFriends().get(key);
                        if (args[1].equals(name)) {
                            toProfile = core.getProfileManager().get(UUID.fromString(key));
                        }
                    }

                    if (toProfile == null) {
                        player.sendMessage("That player is not your friend.");
                        break;
                    }

                    toProfile.getFriends().remove(player.getUniqueId().toString());
                    profile.getFriends().remove(toProfile.getUuid().toString());

                    player.sendMessage(ChatColor.GREEN + "[Friends]" + ChatColor.YELLOW + " Successfully removed " + toProfile.getName() + " from your friends list.");

                    break;

                case "unrequest":
                    if (args.length != 2) {
                        sender.sendMessage(getCommandHelper().getMessage(1));
                        break;
                    }

                    HashSet<String> toRemove = new HashSet<>();
                    try {
                        for (String key : profile.getPendingFriends().keySet()) {
                            String name = profile.getPendingFriends().get(key);
                            if (args[1].equals(name)) {
                                toRemove.add(key);
                            }
                        }
                        toRemove.forEach(string -> profile.getPendingFriends().remove(string));
                    } catch (NullPointerException e) {
                        player.sendMessage("You don't have an out-going friend request to this person.");
                        break;
                    }


                    player.sendMessage(ChatColor.GREEN + "[Friends]" + ChatColor.YELLOW + " Successfully removed friend request to " + args[1]);
                    break;

                default:
                    sender.sendMessage(getCommandHelper().getMessage(1));
            }
        } else {
            sender.sendMessage(getCommandHelper().getMessage(1));
        }
    }
}
