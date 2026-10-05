package me.keano.azurite.modules.timers;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.timers.listeners.playertimers.*;
import me.keano.azurite.modules.timers.listeners.servertimers.AntiRaidTimer;
import me.keano.azurite.modules.timers.listeners.servertimers.RebootTimer;
import me.keano.azurite.modules.timers.listeners.servertimers.TeamRegenTimer;
import me.keano.azurite.modules.timers.listeners.servertimers.anticlean.AntiCleanTimer;
import me.keano.azurite.modules.timers.type.CustomTimer;
import me.keano.azurite.modules.timers.type.PlayerTimer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class TimerManager extends Manager {

    private final Map<String, CustomTimer> customTimers;
    private final Map<String, PlayerTimer> playerTimers;

    private final LogoutTimer logoutTimer;
    private final InvincibilityTimer invincibilityTimer;
    private final PvPTimer pvpTimer;
    private final WarmupTimer warmupTimer;
    private final CombatTimer combatTimer;
    private final AppleTimer appleTimer;
    private final EnderpearlTimer enderpearlTimer;
    private final RodTimer rodTimer;
    private final GappleTimer gappleTimer;
    private final ArcherTagTimer archerTagTimer;
    private final StuckTimer stuckTimer;
    private final SpawnTimer spawnTimer;
    private final HQTimer hqTimer;
    private final CampTimer campTimer;
    private final AntiCleanTimer antiCleanTimer;

    private final DeathbanTimer deathbanTimer;
    private final TeamRegenTimer teamRegenTimer;
    private final AntiRaidTimer antiRaidTimer;
    private final RebootTimer rebootTimer;

    public TimerManager(HCF instance) {
        super(instance);

        this.customTimers = new LinkedHashMap<>(); // we need it sorted.
        this.playerTimers = new LinkedHashMap<>(); // we need it sorted.

        this.logoutTimer = new LogoutTimer(this);
        this.invincibilityTimer = new InvincibilityTimer(this);
        this.pvpTimer = new PvPTimer(this);
        this.warmupTimer = new WarmupTimer(this);
        this.combatTimer = new CombatTimer(this);
        this.appleTimer = new AppleTimer(this);
        this.enderpearlTimer = new EnderpearlTimer(this);
        this.rodTimer = new RodTimer(this);
        this.gappleTimer = new GappleTimer(this);
        this.archerTagTimer = new ArcherTagTimer(this);
        this.stuckTimer = new StuckTimer(this);
        this.spawnTimer = new SpawnTimer(this);
        this.hqTimer = new HQTimer(this);
        this.campTimer = new CampTimer(this);
        this.antiCleanTimer = new AntiCleanTimer(this);

        this.deathbanTimer = new DeathbanTimer(this);
        this.teamRegenTimer = new TeamRegenTimer(this);
        this.antiRaidTimer = new AntiRaidTimer(this);
        this.rebootTimer = new RebootTimer(this);
    }

    @Override
    public void reload() {
        for (PlayerTimer timer : playerTimers.values()) {
            timer.reload();
            timer.fetchData();
        }

        antiCleanTimer.reload();
        teamRegenTimer.reload();
    }

    public PlayerTimer getPlayerTimer(String name) {
        for (PlayerTimer timer : playerTimers.values()) {
            if (timer.getName().equalsIgnoreCase(name)) return timer;
        }
        return null;
    }

    public CustomTimer getCustomTimer(String name) {
        for (CustomTimer timer : customTimers.values()) {
            if (timer.getName().equalsIgnoreCase(name)) return timer;
        }
        return null;
    }
}