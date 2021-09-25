package cc.kitpvp.core.server;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.profiles.Profile;
import cc.kitpvp.core.utils.Colors;

import java.util.Map;

public enum WhitelistMode {
    NONE("none") {
        @Override
        public boolean allowLogin(Profile profile) {
            return true;
        }
    },

    DONOR("donors") {
        @Override
        public boolean allowLogin(Profile profile) {
            Map<String, Boolean> map = profile.getHighestRank().getAllPermissions(Core.get().getConfig().getString("general.server_category"));
            if (map == null) return false;

            return map.containsKey("core.donor");
        }
    },

    STAFF("staff") {
        @Override
        public boolean allowLogin(Profile profile) {
            Map<String, Boolean> map = profile.getHighestRank().getAllPermissions(Core.get().getConfig().getString("general.server_category"));
            if (map == null) return false;

            return map.containsKey("core.staff");
        }
    };


    private final String name;
    private final String WHITELIST_MESSAGE;

    WhitelistMode(final String name) {
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
            case STAFF:
                return 3;
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

    public void activate() {
        Core.get().getServer().getOnlinePlayers().forEach(player -> {
            Profile profile = Core.get().getProfileManager().get(player.getUniqueId());
            if (!allowLogin(profile)) {
                player.kickPlayer(WHITELIST_MESSAGE);
            }
        });
    }

    public abstract boolean allowLogin(Profile profile);
}