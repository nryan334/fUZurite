package me.keano.azurite.modules.loggers.task;

import me.keano.azurite.modules.loggers.Logger;
import me.keano.azurite.modules.loggers.LoggerManager;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class LoggerRemoveTask extends BukkitRunnable {

    private final LoggerManager manager;
    private final UUID uuid;

    public LoggerRemoveTask(LoggerManager manager, UUID uuid) {
        this.manager = manager;
        this.uuid = uuid;
        this.runTaskLater(manager.getInstance(), manager.getConfig().getInt("LOGGERS.DESPAWN") * 20L);
    }

    @Override
    public void run() {
        Logger logger = manager.getLoggers().remove(uuid);

        if (logger != null) {
            logger.setRemoveTask(null);
            Villager villager = logger.getVillager();
            if (villager != null) villager.remove();
        }
    }
}