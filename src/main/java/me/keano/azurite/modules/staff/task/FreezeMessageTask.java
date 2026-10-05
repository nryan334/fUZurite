package me.keano.azurite.modules.staff.task;

import me.keano.azurite.modules.staff.StaffManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class FreezeMessageTask extends BukkitRunnable {

    private final StaffManager manager;
    private final Player player;

    public FreezeMessageTask(StaffManager manager, Player player) {
        this.manager = manager;
        this.player = player;
        this.runTaskTimer(manager.getInstance(), 0L, 20L * manager.getConfig().getInt("STAFF_MODE.FREEZE_MESSAGE_INTERVAL"));
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cancel();
            return;
        }

        for (String s : manager.getLanguageConfig().getStringList("STAFF_MODE.VANISH_INTERVAL_MESSAGE")) {
            player.sendMessage(s);
        }
    }
}