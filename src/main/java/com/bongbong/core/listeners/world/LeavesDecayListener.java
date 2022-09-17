package com.bongbong.core.listeners.world;

import com.bongbong.core.CorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;

public class LeavesDecayListener implements Listener {

    public LeavesDecayListener(CorePlugin plugin) {
        plugin.registerListener(this);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }
}
