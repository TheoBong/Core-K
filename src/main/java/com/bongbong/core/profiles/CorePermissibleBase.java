package com.bongbong.core.profiles;

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
        if (isOp()) return true;

        for(PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            String p = permission.getPermission().toLowerCase();
            if(p.equals("*")) {
                return true;
            }
        }

        return super.hasPermission(inName);
    }
}
