package me.keano.azurite.modules.ability.task;

import me.keano.azurite.modules.ability.Ability;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeleportTask extends BukkitRunnable {

    private final Runnable finish;
    private final Consumer<Integer> increment;
    private final int max;
    private int i;

    public TeleportTask(Ability ability, Runnable finish, Consumer<Integer> increment, int max) {
        this.finish = finish;
        this.increment = increment;
        this.max = max;
        this.i = 0;
        this.runTaskTimer(ability.getInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        if (i == max) {
            finish.run();
            cancel();
            return;
        }

        increment.accept(i);
        i++;
    }
}