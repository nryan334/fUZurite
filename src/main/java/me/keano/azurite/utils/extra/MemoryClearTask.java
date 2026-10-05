package me.keano.azurite.utils.extra;

import me.keano.azurite.HCF;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class MemoryClearTask extends BukkitRunnable {

    private final HCF instance;

    public MemoryClearTask(HCF instance) {
        this.instance = instance;
        this.runTaskTimer(instance, 0L, (20 * 60) * 5);
    }

    @Override
    public void run() {
        for (Cooldown cooldown : instance.getCooldowns()) {
            cooldown.clean();
        }

        for (TeamCooldown teamCooldown : instance.getTeamCooldowns()) {
            teamCooldown.clean();
        }

        for (AzuriteDecimalFormat decimalFormat : instance.getDecimalFormats()) {
            decimalFormat.clean();
        }

        for (AzuriteDateFormat dateFormat : instance.getDateFormats()) {
            dateFormat.clean();
        }
    }
}