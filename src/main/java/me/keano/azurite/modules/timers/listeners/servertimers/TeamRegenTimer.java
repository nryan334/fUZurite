package me.keano.azurite.modules.timers.listeners.servertimers;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.timers.Timer;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.utils.Tasks;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamRegenTimer extends Timer {

    private final Map<UUID, Long> teamsRegenerating;

    public TeamRegenTimer(TimerManager manager) {
        super(
                manager,
                "RegenTimer",
                "", // none
                ""
        );
        this.teamsRegenerating = new ConcurrentHashMap<>();
        Tasks.executeScheduledAsync(getManager(), 20, this::tick); // can be async because we're just checking
    }

    private void tick() {
        Iterator<UUID> iterator = teamsRegenerating.keySet().iterator();

        while (iterator.hasNext()) {
            PlayerTeam pt = getInstance().getTeamManager().getPlayerTeam(iterator.next());

            if (pt == null) {
                iterator.remove(); // They disbanded
                continue;
            }

            if (hasTimer(pt)) continue; // if they have the timer continue looping

            iterator.remove();
            new MinuteRegenTask(getInstance(), pt);
            pt.setMinuteRegen(true);
        }
    }

    public void applyTimer(PlayerTeam pt) {
        int seconds = getTeamConfig().getInt("TEAM_DTR.REGEN_TIMER") * 60;
        teamsRegenerating.put(pt.getUniqueID(), System.currentTimeMillis() + (seconds * 1000L));
        pt.setMinuteRegen(false); // don't regen if they have the timer
    }

    public void applyTimer(PlayerTeam pt, long time) {
        teamsRegenerating.put(pt.getUniqueID(), System.currentTimeMillis() + time);
        pt.setMinuteRegen(false); // don't regen if they have the timer
    }

    public void removeTimer(PlayerTeam pt) {
        teamsRegenerating.remove(pt.getUniqueID());
    }

    public boolean hasTimer(PlayerTeam pt) {
        return getRemaining(pt) > 0L;
    }

    public long getRemaining(PlayerTeam pt) {
        Long fetch = teamsRegenerating.get(pt.getUniqueID());

        if (fetch == null) {
            return 0L;
        }

        return fetch - System.currentTimeMillis();
    }

    public void startMinuteRegen(PlayerTeam pt) {
        if (pt.isMinuteRegen()) {
            new MinuteRegenTask(getInstance(), pt);
        }
    }

    private static class MinuteRegenTask extends BukkitRunnable {

        private final HCF instance;
        private final PlayerTeam pt;

        public MinuteRegenTask(HCF instance, PlayerTeam pt) {
            this.instance = instance;
            this.pt = pt;
            this.runTaskTimer(instance, 0L, instance.getTimerManager()
                    .getTeamConfig().getInt("TEAM_DTR.REGEN_DTR_INTERVAL") * 20L);
        }

        @Override
        public void run() {
            // they disband while regenerating
            if (!instance.getTeamManager().getTeams().containsKey(pt.getUniqueID())) {
                cancel();
                return;
            }

            // Someone died while regenerating
            if (instance.getTimerManager().getTeamRegenTimer().hasTimer(pt)) {
                cancel();
                pt.setMinuteRegen(false);
                return;
            }

            pt.setMinuteRegen(true);
            pt.setDtr(pt.getDtr() + Config.DTR_REGEN_PER_MIN);
            pt.save();
            pt.broadcast(instance.getTimerManager().getLanguageConfig().getString("TEAM_REGEN_TIMER.REGENERATING")
                    .replace("%dtr%", String.valueOf(Config.DTR_REGEN_PER_MIN))
            );

            // Check this after incrementing.
            if (pt.getDtr() >= pt.getMaxDtr()) {
                cancel();
                pt.setDtr(pt.getMaxDtr());
                pt.setMinuteRegen(false);
                pt.broadcast(instance.getTimerManager().getLanguageConfig().getString("TEAM_REGEN_TIMER.FINISHED_REGENERATING"));
                pt.save();
            }
        }
    }
}