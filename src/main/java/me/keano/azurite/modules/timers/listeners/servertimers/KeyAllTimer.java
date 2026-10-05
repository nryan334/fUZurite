package me.keano.azurite.modules.timers.listeners.servertimers;

import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.type.CustomTimer;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Bukkit;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KeyAllTimer extends CustomTimer {

    private final String command;

    public KeyAllTimer(TimerManager manager, String name, String displayName, long time, String command) {
        super(manager, name, displayName, time);
        this.command = command;
    }

    @Override
    public String getRemainingString() {
        long rem = remaining - System.currentTimeMillis();

        if (rem < 0L) {
            getManager().getCustomTimers().remove(name);
            Tasks.execute(getManager(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        }

        return Formatter.getRemaining(rem, false);
    }
}