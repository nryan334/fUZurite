package me.keano.azurite.modules.pvpclass.type.rogue;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class RogueBackstabEvent extends EntityDamageEvent {

    private Player backstabbedBy;

    public RogueBackstabEvent(Entity damagee, Player backstabbedBy, DamageCause cause, double damage) {
        super(damagee, cause, damage);
        this.backstabbedBy = backstabbedBy;
    }
}