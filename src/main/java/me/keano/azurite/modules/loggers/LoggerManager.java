package me.keano.azurite.modules.loggers;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.loggers.listener.LoggerListener;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class LoggerManager extends Manager {

    private final Map<UUID, Logger> loggers; // villager uuid, logger
    private final Map<UUID, Logger> playerLoggers; // player uuid, logger

    public LoggerManager(HCF instance) {
        super(instance);
        this.loggers = new ConcurrentHashMap<>();
        this.playerLoggers = new ConcurrentHashMap<>();
        new LoggerListener(this);
    }

    @Override
    public void disable() {
        this.removeAll();
    }

    @Override
    public void enable() {
        Tasks.executeLater(this, 20, this::removeAll);
    }

    private void removeAll() {
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity livingEntity : world.getLivingEntities()) {
                if (isLogger(livingEntity)) livingEntity.remove();
            }
        }
    }

    public boolean isLogger(LivingEntity entity) {
        return entity.getCustomName() != null && entity.getCustomName().endsWith("Logger");
    }

    public Logger getByPlayer(UUID player) {
        return playerLoggers.get(player);
    }
}