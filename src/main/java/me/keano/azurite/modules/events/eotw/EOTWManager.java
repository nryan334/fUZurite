package me.keano.azurite.modules.events.eotw;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.events.eotw.listener.EOTWListener;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.timers.type.CustomTimer;
import me.keano.azurite.utils.Formatter;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class EOTWManager extends Manager {

    private final Set<UUID> whitelisted;
    private boolean active;

    public EOTWManager(HCF instance) {
        super(instance);

        this.whitelisted = new HashSet<>();
        this.active = false;

        new EOTWListener(this);
    }

    public void startPreEOTW(long time) {
        new CustomTimer(getInstance().getTimerManager(), "EOTW", "EOTW", time);

        for (String s : getLanguageConfig().getStringList("EOTW_TIMER.STARTED_PRE_EOTW")) {
            Bukkit.broadcastMessage(s);
        }
    }

    public boolean isNotPreEOTW() {
        return getInstance().getTimerManager().getCustomTimer("EOTW") == null;
    }

    public void setRaidable() {
        long time = Formatter.parse(getConfig().getString("EOTW_TIMER.RAIDABLE_TIME"));

        for (Team team : getInstance().getTeamManager().getTeams().values()) {
            if (team instanceof PlayerTeam) {
                PlayerTeam pt = (PlayerTeam) team;
                getInstance().getTimerManager().getTeamRegenTimer().applyTimer(pt, time);
                pt.setDtr(-100);
                pt.save();
            }
        }
    }
}