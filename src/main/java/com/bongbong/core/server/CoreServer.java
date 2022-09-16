package com.bongbong.core.server;

import lombok.Data;
import org.bukkit.configuration.file.FileConfiguration;

public @Data class CoreServer {

    private int chatCooldown;
    private WhitelistMode whitelistMode = WhitelistMode.NONE;
    private boolean chatMuted;

    public CoreServer(FileConfiguration config) {
        this.chatCooldown = config.getInt("general.chat_cooldown");
        this.whitelistMode = WhitelistMode.getModeById(config.getInt("general.whitelist_mode"));
        this.chatMuted = config.getBoolean("general.chat_muted");
    }

    public void export(FileConfiguration config) {
        config.set("general.chat_cooldown", chatCooldown);
        config.set("general.whitelist_mode", WhitelistMode.getIdByMode(whitelistMode));
        config.set("general.chat_muted", chatMuted);
    }
}
