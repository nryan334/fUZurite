package me.keano.azurite.modules.ability.event;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.ability.Ability;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class AbilityUseEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Ability ability;
    private Player player;
    private long cooldown;

    public AbilityUseEvent(Ability ability, Player player, long cooldown) {
        this.ability = ability;
        this.player = player;
        this.cooldown = cooldown;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}