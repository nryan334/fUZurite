package me.keano.azurite.modules.timers.listeners.playertimers;

import lombok.Getter;
import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.type.PlayerTimer;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class AbilityTimer extends PlayerTimer {

    private final Ability ability;

    public AbilityTimer(TimerManager manager, Ability ability, String scoreboardPath) {
        super(
                manager,
                null,
                false,
                ability.getName().replaceAll(" ", ""),
                scoreboardPath,
                ability.getNameConfig() + ".COOLDOWN"
        );
        this.ability = ability;
    }
}