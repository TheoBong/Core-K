package cc.kitpvp.core.commands.general;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.commands.BaseCommand;
import cc.kitpvp.core.profiles.Profile;
import cc.kitpvp.core.ranks.Rank;
import cc.kitpvp.core.utils.Colors;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ListCommand extends BaseCommand {

    private final Core core;

    public ListCommand(Core core, String name) {
        super(name);
        this.core = core;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {

        List<Profile> profiles = new ArrayList<>();
        for(Profile profile : core.getProfileManager().getProfiles().values()) {
            if(profile.getPlayer() != null && profile.getPlayer().isOnline()) {
                profiles.add(profile);
            }
        }

        profiles.sort(Comparator.comparing(Profile::getName));

        ArrayList<Rank> ranks = new ArrayList<>(core.getRankManager().getRanks().values());
        ranks.sort(Comparator.comparing(Rank::getWeight).reversed());

        StringBuilder sbRanks = new StringBuilder(), sbPlayers = new StringBuilder();
        sbPlayers.append("&aOnline (" + Bukkit.getOnlinePlayers().size() + "): ");

        while(!ranks.isEmpty()) {
            final Rank rank = ranks.get(0);
            List<Profile> list = new ArrayList<>(profiles);
            for(Profile profile : list) {
                if(profile.getHighestRank().equals(rank)) {
                    sbPlayers.append(rank.getColor() + profile.getName());
                    profiles.remove(profile);
                    if(profiles.size() > 0) {
                        sbPlayers.append("&7, ");
                    } else {
                        sbPlayers.append("&7.");
                    }
                }
            }

            sbRanks.append(rank.getColor() + rank.getDisplayName());
            ranks.remove(rank);
            if(ranks.isEmpty()) {
                sbRanks.append("&7.");
            } else {
                sbRanks.append("&7, ");
            }
        }

        sender.sendMessage(Colors.get(sbRanks.toString()));
        sender.sendMessage(Colors.get(sbPlayers.toString()));
    }
}
