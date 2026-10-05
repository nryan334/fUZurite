package me.keano.azurite.modules.timers.event;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.timers.type.CustomTimer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class CustomTimerExpireEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private CustomTimer timer;

    public CustomTimerExpireEvent(CustomTimer timer) {
        super(true);
        this.timer = timer;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}