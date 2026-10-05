package me.keano.azurite.modules.teams.task;

import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamViewerTask extends BukkitRunnable {

    private final TeamManager manager;
    private final UUID uuid;

    public TeamViewerTask(TeamManager manager, UUID uuid) {
        this.manager = manager;
        this.uuid = uuid;
        this.runTaskTimer(manager.getInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        PlayerTeam team = manager.getPlayerTeam(uuid);

        if (team == null) {
            cancel();
            return;
        }

        List<Player> members = team.getOnlinePlayers(true);

        if (members.isEmpty()) {
            team.setTeamViewerTask(null);
            cancel();
            return;
        }

        for (Player member : members) {
            manager.getInstance().getClientHook().sendTeamViewer(member, team);
        }
    }
}