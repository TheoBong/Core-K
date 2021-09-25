package cc.kitpvp.core.utils;

import cc.kitpvp.core.Core;

public class ThreadUtil {
    public static void runTask(boolean async, Core core, Runnable runnable) {
        if(async) {
            core.getServer().getScheduler().runTaskAsynchronously(core, runnable);
        } else {
            runnable.run();
        }
    }
}
