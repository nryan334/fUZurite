package me.keano.azurite.modules.events.koth.task;

import me.keano.azurite.modules.events.koth.Koth;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KothTickTask extends BukkitRunnable {

    private final Koth koth;

    public KothTickTask(Koth koth) {
        this.koth = koth;
        this.runTaskTimer(koth.getInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        if (!koth.isActive()) {
            cancel();
            return;
        }

        koth.tick();
    }
}