package me.keano.azurite.modules.pvpclass.cooldown;

import lombok.Getter;
import me.keano.azurite.modules.pvpclass.PvPClass;
import me.keano.azurite.utils.extra.Cooldown;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class CustomCooldown extends Cooldown {

    private final String displayName;

    public CustomCooldown(PvPClass pvpClass, String displayName) {
        super(pvpClass.getManager());
        this.displayName = (displayName.isEmpty() ? null : displayName);
        pvpClass.getCustomCooldowns().add(this);
    }
}