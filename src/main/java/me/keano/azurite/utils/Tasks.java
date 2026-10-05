package me.keano.azurite.utils;

import me.keano.azurite.modules.framework.Manager;
import org.bukkit.Bukkit;

public class Tasks {

    public static void executeAsync(Manager manager, Runnable runnable) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(manager.getInstance(), runnable);
    }

    public static void execute(Manager manager, Runnable runnable) {
        Bukkit.getServer().getScheduler().runTask(manager.getInstance(), runnable);
    }

    public static void executeLater(Manager manager, long ticks, Runnable runnable) {
        Bukkit.getServer().getScheduler().runTaskLater(manager.getInstance(), runnable, ticks);
    }

    public static void executeLaterAsync(Manager manager, long ticks, Runnable runnable) {
        Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(manager.getInstance(), runnable, ticks);
    }

    public static void executeScheduledAsync(Manager manager, long ticks, Runnable runnable) {
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(manager.getInstance(), runnable, 0L, ticks);
    }

    public static void executeScheduled(Manager manager, long ticks, Runnable runnable) {
        Bukkit.getServer().getScheduler().runTaskTimer(manager.getInstance(), runnable, 0L, ticks);
    }
}