package me.keano.azurite.modules.timers.listeners.servertimers.anticlean;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.timers.TimerManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class AntiCleanTask extends BukkitRunnable {

    private final TimerManager manager;
    private final UUID team;
    private final UUID target;

    private long time;

    public AntiCleanTask(TimerManager manager, UUID team, UUID target, int seconds) {
        this.manager = manager;
        this.team = team;
        this.target = target;
        this.time = System.currentTimeMillis() + (1000L * seconds);
        this.runTaskTimer(manager.getInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        PlayerTeam pt = manager.getInstance().getTeamManager().getPlayerTeam(team);
        PlayerTeam ptTarget = manager.getInstance().getTeamManager().getByPlayer(target);

        if (pt == null) {
            cancel();

            if (ptTarget != null && ptTarget.getAntiCleanTask() != null) {
                ptTarget.getAntiCleanTask().cancel();
                ptTarget.setAntiCleanTask(null);
            }
            return;
        }

        long remaining = getRemaining();

        if (remaining < 0L) {
            cancel();
            pt.setAntiCleanTask(null);
            manager.getAntiCleanTimer().sendStats(pt);
        }
    }

    public void updateTime(int seconds) {
        this.time = System.currentTimeMillis() + (seconds * 1000L);
    }

    public long getRemaining() {
        return time - System.currentTimeMillis();
    }
}