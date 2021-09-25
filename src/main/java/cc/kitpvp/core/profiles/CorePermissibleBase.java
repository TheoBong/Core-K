package cc.kitpvp.core.profiles;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class CorePermissibleBase extends PermissibleBase {

    private Player player;

    public CorePermissibleBase(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public boolean hasPermission(String inName) {
        boolean b = isOp() || super.hasPermission(inName);
        String perm = inName.toLowerCase();

        for(PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            String p = permission.getPermission().toLowerCase();
            if(p.endsWith("*")) {
                String subP = perm.substring(0, perm.length() == 1 ? 0 : perm.length() - 2);
                if(perm.startsWith(subP)) {
                    b = true;
                }
            }
        }

        return b;
    }
}
