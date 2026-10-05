package me.keano.azurite.modules.tablist.extra;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.events.koth.Koth;
import me.keano.azurite.modules.scheduler.extra.NextSchedule;
import me.keano.azurite.modules.tablist.Tablist;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class TablistData {

    private Tablist info;
    private User user;
    private PlayerTeam pt;
    private Team atPlayer;
    private NextSchedule schedule;
    private String teamListString;
    private String line;

    private Player player;
    private List<PlayerTeam> teamList;
    private List<Koth> activeKoths;

    private int staff;

    public TablistData(Tablist info, User user, PlayerTeam pt, Team atPlayer, NextSchedule schedule, String teamListString,
                       Player player, List<PlayerTeam> teamList, List<Koth> activeKoths, int staff) {
        this.info = info;
        this.user = user;
        this.pt = pt;
        this.atPlayer = atPlayer;
        this.schedule = schedule;
        this.teamListString = teamListString;
        this.player = player;
        this.teamList = teamList;
        this.activeKoths = activeKoths;
        this.staff = staff;
        this.line = null;
    }
}