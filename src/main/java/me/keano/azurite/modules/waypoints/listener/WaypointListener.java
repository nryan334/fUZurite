package me.keano.azurite.modules.waypoints.listener;

import me.keano.azurite.modules.events.conquest.Conquest;
import me.keano.azurite.modules.events.conquest.extra.Capzone;
import me.keano.azurite.modules.events.koth.Koth;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.enums.MountainType;
import me.keano.azurite.modules.teams.enums.TeamType;
import me.keano.azurite.modules.teams.type.MountainTeam;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.waypoints.WaypointManager;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.function.UnaryOperator;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class WaypointListener extends Module<WaypointManager> {

    public WaypointListener(WaypointManager manager) {
        super(manager);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (getInstance().getClientHook().getClients().isEmpty()) return;
        Player player = e.getPlayer();
        Tasks.executeLater(getManager(), 20 * 3L, () -> this.sendWaypoints(player));
    }

    @EventHandler
    public void onChange(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();

        switch (e.getFrom().getEnvironment()) {
            case NORMAL:
                getManager().getSpawnWaypoint().remove(player, getManager().getWorldSpawn(), UnaryOperator.identity());
                break;

            case THE_END:
                getManager().getEndSpawnWaypoint().remove(player, getManager().getEndSpawn(), UnaryOperator.identity());
                break;

            case NETHER:
                getManager().getNetherSpawnWaypoint().remove(player, getManager().getNetherSpawn(), UnaryOperator.identity());
                break;
        }

        switch (player.getWorld().getEnvironment()) {
            case NORMAL:
                getManager().getSpawnWaypoint().send(player, getManager().getWorldSpawn(), UnaryOperator.identity());
                break;

            case THE_END:
                getManager().getEndSpawnWaypoint().send(player, getManager().getEndSpawn(), UnaryOperator.identity());
                break;

            case NETHER:
                getManager().getNetherSpawnWaypoint().send(player, getManager().getNetherSpawn(), UnaryOperator.identity());
                break;
        }
    }

    private void sendWaypoints(Player player) {
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
        Conquest conquest = getInstance().getConquestManager().getConquest();

        switch (player.getWorld().getEnvironment()) {
            case NORMAL:
                getManager().getSpawnWaypoint().send(player, getManager().getWorldSpawn(), UnaryOperator.identity());
                break;

            case THE_END:
                getManager().getEndSpawnWaypoint().send(player, getManager().getEndSpawn(), UnaryOperator.identity());
                break;

            case NETHER:
                getManager().getNetherSpawnWaypoint().send(player, getManager().getNetherSpawn(), UnaryOperator.identity());
                break;
        }

        getInstance().getClientHook().handleJoin(player);
        getManager().getEndExitWaypoint().send(player, getManager().getEndExit(), UnaryOperator.identity());
        checkSystemTeams(player);

        for (Koth koth : getInstance().getKothManager().getKoths().values()) {
            if (koth.getCaptureZone() == null) continue;
            if (!koth.isActive()) continue;
            getManager().getKothWaypoint().send(player, koth.getCaptureZone().getCenter(), s -> s.replace("%name%", koth.getName()));
        }

        if (conquest.isActive()) {
            for (Capzone capzone : conquest.getCapzones().values()) {
                getManager().getConquestWaypoint().send(player, capzone.getZone().getCenter(), s -> s
                        .replace("%name%", capzone.getType().getName())
                        .replace("%color%", String.valueOf(Utils.translateChatColorToColor(capzone.getType().getColor()).getRGB())));
            }
        }

        if (pt != null) {
            getManager().getHqWaypoint().send(player, pt.getHq(), UnaryOperator.identity());
            getManager().getRallyWaypoint().send(player, pt.getRallyPoint(), UnaryOperator.identity());

            if (pt.getFocus() != null) {
                Team focusTeam = pt.getFocusedTeam();

                if (focusTeam != null) {
                    getManager().getFocusWaypoint().send(player, focusTeam.getHq(), s -> s.replace("%team%", focusTeam.getName()));
                }
            }
        }
    }

    private void checkSystemTeams(Player player) {
        for (Team team : getInstance().getTeamManager().getSystemTeams().values()) {
            if (team.getType() != TeamType.MOUNTAIN) continue;

            MountainTeam mt = (MountainTeam) team;

            if (mt.getMountainType() == MountainType.GLOWSTONE) {
                getManager().getGlowstoneWaypoint().send(player, mt.getHq(), UnaryOperator.identity());

            } else {
                getManager().getOreMountainWaypoint().send(player, mt.getHq(), UnaryOperator.identity());
            }
        }
    }
}