package me.keano.azurite.modules.walls;

import lombok.Getter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.claims.Claim;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class Wall extends Module<WallManager> {

    private final Set<Location> walls;
    private final Set<Location> removing;
    private final Set<Location> teamMap;
    private final Set<Claim> lunar;

    public Wall(WallManager manager) {
        super(manager);
        this.walls = new HashSet<>();
        this.removing = new HashSet<>();
        this.teamMap = new HashSet<>();
        this.lunar = new HashSet<>();
    }

    public void tick(Player player) {
        Set<Claim> nearby = getInstance().getTeamManager().getClaimManager().getNearbyCuboids(player.getLocation(), 6);

        // Clear past blocks that are out of range
        getManager().clearWalls(player);
        getManager().clearLunarBorders(player, nearby);

        for (Claim claim : nearby) {
            Team team = getInstance().getTeamManager().getTeam(claim.getTeam());
            if (team == null) continue;

            WallType wallType = getManager().getWallType(claim, team, player);
            if (wallType == null) continue;

            getManager().sendWall(player, claim, wallType);

            if (wallType.isEntryLimited()) {
                if (getManager().getCooldown().hasCooldown(player)) continue;

                String deniedMessage = getLanguageConfig().getString("WALL_LISTENER.DENIED_" + wallType.getConfigPath() + "_ENTRY")
                        .replace("%members%", String.valueOf(wallType.getEntryLimit()))
                        .replace("%claim%", team.getDisplayName(player));

                player.sendMessage(deniedMessage);
                getManager().getCooldown().applyCooldown(player, 3);
            }
        }
    }
}