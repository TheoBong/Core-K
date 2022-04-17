package cc.kitpvp.core.papi;

import cc.kitpvp.core.Core;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class CoreExpansion extends PlaceholderExpansion {
    private final Core plugin;

    public CoreExpansion(Core plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getAuthor() {
        return "Theo";
    }

    @Override
    public String getIdentifier() {
        return "Core";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if(params.equalsIgnoreCase("rank")){
            return plugin.getProfileManager().get(player.getUniqueId()).getHighestRank().getDisplayName();
        }

        if(params.equalsIgnoreCase("color")){
            return plugin.getProfileManager().get(player.getUniqueId()).getHighestRank().getColor();
        }

        if(params.equalsIgnoreCase("prefix")){
            String prefix = plugin.getProfileManager().get(player.getUniqueId()).getHighestRank().getPrefix();
            if (prefix == null) return "";
            return prefix + " ";
        }

        if(params.equalsIgnoreCase("weight")){
            return plugin.getProfileManager().get(player.getUniqueId()).getHighestRank().getWeight() + "";
        }

        return null; // Placeholder is unknown by the expansion
    }
}