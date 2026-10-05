package me.keano.azurite.modules.timers.listeners.playertimers;

import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class DeathbanTimer extends PlayerTimer {

    private final Map<String, Long> deathbanTime;

    public DeathbanTimer(TimerManager manager) {
        super(
                manager,
                null,
                false,
                "Deathban",
                "DEATHBAN_INFO.TIME",
                ""
        );
        this.deathbanTime = new HashMap<>();
        this.load();
    }

    @Override
    public void reload() {
        deathbanTime.clear();
        this.load();
    }

    private void load() {
        for (String key : getConfig().getStringList("DEATHBANS.TIMES")) {
            String[] split = key.split(", ");

            deathbanTime.put(
                    "azurite.deathban." + split[0].toLowerCase(),
                    Integer.parseInt(split[1]) * (1000L * 60L) // it's in mins
            );
        }
    }

    private long getDeathbanTime(Player player) {
        long lowest = getConfig().getInt("DEATHBANS.DEFAULT_TIME") * (1000L * 60L);

        for (Map.Entry<String, Long> entry : deathbanTime.entrySet()) {
            String perm = entry.getKey();
            Long time = entry.getValue();

            if (player.hasPermission(perm) && time < lowest) { // we want the lowest
                lowest = time;
            }
        }

        return lowest;
    }

    @Override
    public void applyTimer(Player player) {
        if (player.hasPermission("azurite.deathban.bypass")) return;
        timerCache.put(player.getUniqueId(), System.currentTimeMillis() + getDeathbanTime(player));
    }
}