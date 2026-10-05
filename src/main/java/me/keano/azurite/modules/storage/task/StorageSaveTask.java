package me.keano.azurite.modules.storage.task;

import me.keano.azurite.modules.storage.StorageManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class StorageSaveTask extends BukkitRunnable {

    private final List<String> commands;
    private final boolean enabled;

    public StorageSaveTask(StorageManager manager) {
        this.commands = manager.getConfig().getStringList("SAVE_DATA_TASK.COMMANDS");
        this.enabled = manager.getConfig().getBoolean("SAVE_DATA_TASK.ENABLED");
        this.runTaskTimer(manager.getInstance(), 20 * 60, 20L * manager.getConfig().getInt("SAVE_DATA_TASK.TIME"));
    }

    @Override
    public void run() {
        if (!enabled) return;

        for (String command : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
}