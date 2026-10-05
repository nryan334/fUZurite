package me.keano.azurite.modules.ability.extra;

import me.keano.azurite.modules.board.extra.ActionBarConfig;
import me.keano.azurite.modules.hooks.abilities.AbilitiesHook;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class GlobalCooldown extends PlayerTimer {

    private final AbilitiesHook abilitiesHook;

    public GlobalCooldown(TimerManager manager) {
        super(
                manager,
                ActionBarConfig.GLOBAL_ABILITY,
                false,
                "PartnerItem",
                "",
                "GLOBAL_ABILITY.COOLDOWN"
        );
        this.abilitiesHook = manager.getInstance().getAbilitiesHook();
    }

    @Override
    public boolean hasTimer(Player player) {
        return super.hasTimer(player) || getInstance().getAbilitiesHook().hasGlobalCooldown(player);
    }

    @Override
    public String getRemainingString(Player player) {
        if (abilitiesHook.hasGlobalCooldown(player)) {
            return abilitiesHook.getRemainingGlobal(player);
        }

        return super.getRemainingString(player);
    }

    @Override
    public String getRemainingStringBoard(Player player) {
        if (abilitiesHook.hasGlobalCooldown(player)) {
            return abilitiesHook.getRemainingGlobal(player);
        }

        return super.getRemainingStringBoard(player);
    }
}