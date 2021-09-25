package cc.kitpvp.core.listeners.world;

import cc.kitpvp.core.Core;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;

public class LeavesDecayListener implements Listener {

    private final Core core;
    public LeavesDecayListener(Core core) {
        this.core = core;
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }
}
