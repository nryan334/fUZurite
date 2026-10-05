package me.keano.azurite.modules.timers.type;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.timers.Timer;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.event.CustomTimerExpireEvent;
import me.keano.azurite.utils.Formatter;
import org.bukkit.Bukkit;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class CustomTimer extends Timer {

    protected Long remaining;
    protected String displayName;

    public CustomTimer(TimerManager manager, String name, String displayName, long time) {
        super(
                manager,
                name,
                "",
                ""
        );
        this.displayName = displayName;
        this.remaining = (System.currentTimeMillis() + time);
        getManager().getCustomTimers().put(name, this);
    }

    public boolean isActive() {
        return remaining > 0L;
    }

    public String getRemainingString() {
        long rem = remaining - System.currentTimeMillis();
        return Formatter.getRemaining(rem, false);
    }

    public void tick() {
        long rem = remaining - System.currentTimeMillis();

        if (rem < 0L) {
            getManager().getCustomTimers().remove(name);
            Bukkit.getPluginManager().callEvent(new CustomTimerExpireEvent(this));
        }
    }
}