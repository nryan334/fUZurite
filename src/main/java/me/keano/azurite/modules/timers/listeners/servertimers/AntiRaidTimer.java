package me.keano.azurite.modules.timers.listeners.servertimers;

import lombok.Getter;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.timers.Timer;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.extra.Pair;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class AntiRaidTimer extends Timer {

    private final Map<UUID, Pair<UUID, Long>> cooldowns;

    public AntiRaidTimer(TimerManager manager) {
        super(
                manager,
                "AntiRaid",
                "",
                "TIMERS_COOLDOWN.ANTI_RAID"
        );
        this.cooldowns = new ConcurrentHashMap<>();
    }

    public void applyTimer(PlayerTeam raidable, PlayerTeam pt) {
        cooldowns.put(raidable.getUniqueID(), new Pair<>(pt.getUniqueID(), System.currentTimeMillis() + (1000L * seconds)));
    }

    public long getRemaining(PlayerTeam raidable, PlayerTeam pt) {
        Pair<UUID, Long> remaining = cooldowns.get(raidable.getUniqueID());

        if (remaining != null) {
            // Time has passed
            if (remaining.getValue() < System.currentTimeMillis()) {
                cooldowns.remove(raidable.getUniqueID());
            }

            // Check if the team name is the same if the time hasnt passed otherwise deny.
            if (pt != null && remaining.getKey().equals(pt.getUniqueID())) {
                return remaining.getValue() - System.currentTimeMillis();
            }
        }

        return 0L;
    }

    public String getRemainingString(PlayerTeam raidable, PlayerTeam pt) {
        return Formatter.getRemaining(getRemaining(raidable, pt), false);
    }

    public boolean canBreak(PlayerTeam raidable, PlayerTeam pt) {
        return getRemaining(raidable, pt) <= 0L;
    }
}