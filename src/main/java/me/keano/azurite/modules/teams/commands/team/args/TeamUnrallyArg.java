package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.waypoints.WaypointAzurite;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.function.UnaryOperator;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamUnrallyArg extends Argument {

    public TeamUnrallyArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "unrally",
                        "removerally"
                )
        );
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (pt == null) {
            sendMessage(sender, Config.NOT_IN_TEAM);
            return;
        }

        if (pt.getRallyPoint() == null) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_UNRALLY.NO_RALLY"));
            return;
        }

        WaypointAzurite rallyWaypoint = getInstance().getWaypointManager().getRallyWaypoint();

        // Azurite - Lunar Integration
        for (Player member : pt.getOnlinePlayers(true)) {
            rallyWaypoint.remove(member, pt.getRallyPoint(), UnaryOperator.identity());
        }

        pt.setRallyPoint(null);
        pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_UNRALLY.UNRALLIED"));
    }
}