package me.keano.azurite.modules.teams.extra;

import lombok.Getter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.modules.users.settings.TeamListSetting;
import me.keano.azurite.utils.Tasks;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class TeamSorting extends Module<TeamManager> {

    private final List<PlayerTeam> teamTop;
    private final List<PlayerTeam> teamTopRaidable;
    private final List<PlayerTeam> lowestDTRSorted;
    private final List<PlayerTeam> highestDTRSorted; // TODO: optimize this.
    private final List<PlayerTeam> highestOnlineSorted;
    private final List<PlayerTeam> lowestOnlineSorted;
    private final ReentrantLock reentrantLock;

    public TeamSorting(TeamManager manager) {
        super(manager);

        this.teamTop = new ArrayList<>();
        this.teamTopRaidable = new ArrayList<>();
        this.lowestDTRSorted = new ArrayList<>();
        this.highestDTRSorted = new ArrayList<>();
        this.highestOnlineSorted = new ArrayList<>();
        this.lowestOnlineSorted = new ArrayList<>();
        this.reentrantLock = new ReentrantLock();

        Tasks.executeScheduledAsync(manager, 20 * 60, this::sort);
    }

    public List<PlayerTeam> getList(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = getInstance().getUserManager().getByUUID(player.getUniqueId());

            switch (user.getTeamListSetting()) {
                case LOWEST_DTR:
                    return lowestDTRSorted;

                case ONLINE_LOW:
                    return lowestOnlineSorted;

                case HIGHEST_DTR:
                    return highestDTRSorted;

                case ONLINE_HIGH:
                    return highestOnlineSorted;
            }
        }

        return lowestOnlineSorted;
    }

    public TeamListSetting getSetting(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
            return user.getTeamListSetting();
        }

        return TeamListSetting.ONLINE_HIGH;
    }

    public void remove(PlayerTeam pt) {
        teamTop.remove(pt);
        teamTopRaidable.remove(pt);
        lowestDTRSorted.remove(pt);
        highestDTRSorted.remove(pt);
        lowestOnlineSorted.remove(pt);
        highestOnlineSorted.remove(pt);
    }

    public void removeList(PlayerTeam pt) {
        lowestDTRSorted.remove(pt);
        highestDTRSorted.remove(pt);
        lowestOnlineSorted.remove(pt);
        highestOnlineSorted.remove(pt);
    }

    public void sort() {
        if (reentrantLock.isLocked()) return;

        try {
            reentrantLock.lock();

            // Can just use one list and sort that for each.
            List<PlayerTeam> sorted = new ArrayList<>();

            for (Team team : getInstance().getTeamManager().getTeams().values()) {
                if (!(team instanceof PlayerTeam)) continue;
                sorted.add((PlayerTeam) team);
            }

            sorted.sort(Comparator.comparingInt(PlayerTeam::getPoints).reversed());
            teamTop.clear();
            teamTop.addAll(sorted);

            sorted.sort(Comparator.comparingInt(PlayerTeam::getRaidablePoints).reversed());
            teamTopRaidable.clear();
            teamTopRaidable.addAll(sorted);

            // We can now remove all the teams who aren't online since it's not needed for team lists
            sorted.removeIf(playerTeam -> playerTeam.getOnlinePlayers(false).isEmpty());

            // Sort low -> high
            sorted.sort(Comparator.comparingDouble(PlayerTeam::getDtr));
            lowestDTRSorted.clear();
            lowestDTRSorted.addAll(sorted);

            // Sort high -> low
            sorted.sort(Comparator.comparingDouble(PlayerTeam::getDtr).reversed());
            highestDTRSorted.clear();
            highestDTRSorted.addAll(sorted);

            // Sort low -> high
            sorted.sort(Comparator.comparingInt(pt -> pt.getOnlinePlayersSize(false)));
            lowestOnlineSorted.clear();
            lowestOnlineSorted.addAll(sorted);

            // Sort high -> low
            sorted.sort(Comparator.comparingInt(pt -> pt.getOnlinePlayersSize(false)));
            Collections.reverse(sorted);
            highestOnlineSorted.clear();
            highestOnlineSorted.addAll(sorted);

        } finally {
            reentrantLock.unlock();
        }
    }
}