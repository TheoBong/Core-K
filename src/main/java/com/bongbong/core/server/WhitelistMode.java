package com.bongbong.core.server;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.profiles.Profile;
import com.bongbong.core.utils.Colors;

import java.util.Map;

public enum WhitelistMode {
    NONE("none") {
        @Override
        public boolean allowLogin(CorePlugin plugin, Profile profile) {
            return true;
        }
    },

    DONOR("donors") {
        @Override
        public boolean allowLogin(CorePlugin plugin, Profile profile) {
            Map<String, Boolean> map = profile.getHighestRank().getAllPermissions(plugin.getConfig().getString("general.server_category"));
            if (map == null) return false;

            return map.containsKey("core.donor");
        }
    },
    VIP("partners") {
        @Override
        public boolean allowLogin(CorePlugin plugin, Profile profile) {
            Map<String, Boolean> map = profile.getHighestRank().getAllPermissions(plugin.getConfig().getString("general.server_category"));
            if (map == null) return false;

            return map.containsKey("core.vip");
        }
    },

    STAFF("staff") {
        @Override
        public boolean allowLogin(CorePlugin plugin, Profile profile) {
            Map<String, Boolean> map = profile.getHighestRank().getAllPermissions(plugin.getConfig().getString("general.server_category"));
            if (map == null) return false;

            return map.containsKey("core.staff");
        }
    };
    
    private final String name;
    private final String WHITELIST_MESSAGE;

    WhitelistMode(String name) {
        this.name = name;
        this.WHITELIST_MESSAGE = Colors.get("&cThe server has been whitelisted for " + getName() + " only.");
    }

    public static WhitelistMode getModeById(final int id) {
        switch (id) {
            case 1:
                return NONE;
            case 2:
                return DONOR;
            case 3:
                return VIP;
            case 4:
                return STAFF;
            default:
                return NONE;
        }
    }

    public static int getIdByMode(final WhitelistMode whitelistMode) {
        switch (whitelistMode) {
            case NONE:
                return 1;
            case DONOR:
                return 2;
            case VIP:
                return 3;
            case STAFF:
                return 4;
            default:
                return 0;
        }
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return WHITELIST_MESSAGE;
    }

    public void activate(CorePlugin plugin) {
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            Profile profile = plugin.getProfileManager().get(player.getUniqueId());
            if (!allowLogin(plugin, profile)) {
                player.kickPlayer(WHITELIST_MESSAGE);
            }
        });
    }

    public abstract boolean allowLogin(CorePlugin plugin, Profile profile);
}