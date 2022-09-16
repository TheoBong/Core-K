package com.bongbong.core.utils;

import com.bongbong.core.CorePlugin;

public class ThreadUtil {
    public static void runTask(boolean async, CorePlugin plugin, Runnable runnable) {
        if(async) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
        } else {
            runnable.run();
        }
    }
}
