package me.keano.azurite.modules.pvpclass.cooldown;

import lombok.Getter;
import me.keano.azurite.modules.pvpclass.PvPClass;
import org.bukkit.potion.PotionEffect;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class ClassBuff extends CustomCooldown {

    private final PotionEffect effect;
    private final int cooldown;

    public ClassBuff(PvPClass pvpClass, String displayName, PotionEffect effect, int cooldown) {
        super(pvpClass, displayName);
        this.effect = effect;
        this.cooldown = cooldown;
    }
}