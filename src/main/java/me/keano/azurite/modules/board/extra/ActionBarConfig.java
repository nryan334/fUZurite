package me.keano.azurite.modules.board.extra;

import lombok.Getter;
import me.keano.azurite.modules.framework.Manager;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public enum ActionBarConfig {

    STATS,
    CLAIM,
    COMBAT,
    APPLE,
    ENDERPEARL,
    PVP_TIMER,
    INVINCIBILITY,
    LOGOUT,
    ROD,
    F_STUCK,
    WARMUP,
    HQ_TIMER,
    GLOBAL_ABILITY,
    ANTI_CLEAN,
    SPAWN_TIMER,
    ARCHER_TAG,
    CAMP_TIMER,
    SOTW_TIMER,
    SOTW_TIMER_ENABLED,
    CLASS_NAME,
    BARD_ENERGY,
    BARD_EFFECT,
    MAGE_ENERGY,
    MAGE_EFFECT,
    REBOOT;

    private String line;

    public boolean isEnabled() {
        return !line.isEmpty();
    }

    public void cache(Manager manager) {
        this.line = manager.getScoreboardConfig().getString("ACTION_BAR_CONFIG.LINES." + this.name());
    }
}